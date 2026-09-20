package com.mbh.initio.system;

import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.EnvironmentVariableStatus;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.PortAvailability;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortObservation;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.ServiceStatus;
import com.mbh.initio.model.VerificationState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class LocalEnvironmentInspector {

	private final RuntimeInspector runtimeInspector;
	private final DockerInspector dockerInspector;
	private final PortInspector portInspector;

	public LocalEnvironmentInspector(
			RuntimeInspector runtimeInspector,
			DockerInspector dockerInspector,
			PortInspector portInspector
	) {
		this.runtimeInspector = Objects.requireNonNull(runtimeInspector, "runtimeInspector");
		this.dockerInspector = Objects.requireNonNull(dockerInspector, "dockerInspector");
		this.portInspector = Objects.requireNonNull(portInspector, "portInspector");
	}

	public LocalEnvironmentAnalysis inspect(ProjectAnalysis project) {
		Map<String, InstalledRuntime> runtimes = new LinkedHashMap<>();
		for (RuntimeRequirement requirement : project.runtimeRequirements()) {
			String runtime = requirement.runtime().toLowerCase();
			if (runtimes.containsKey(runtime)) {
				continue;
			}
			runtimes.put(runtime, inspectRuntime(runtime));
		}
		if (project.serviceRequirements().stream().anyMatch(ServiceRequirement::composeBacked)
				&& !runtimes.containsKey("docker")) {
			runtimes.put("docker", inspectRuntime("docker"));
		}

		EnvironmentProvider environmentProvider = new DefaultEnvironmentProvider(project.projectPath());
		List<EnvironmentVariableStatus> environmentStatuses = new ArrayList<>();
		for (EnvironmentVariableRequirement requirement : project.environmentVariableRequirements()) {
			environmentStatuses.add(environmentProvider.inspect(requirement.name()));
		}

		List<ServiceStatus> serviceStatuses = new ArrayList<>(dockerInspector.inspectServices(project));
		List<Integer> ports = project.portExpectations().stream()
				.map(PortExpectation::port)
				.toList();
		List<PortObservation> portObservations = portInspector.inspectPorts(ports);
		addConfiguredServiceStatuses(project, serviceStatuses, portObservations);

		return new LocalEnvironmentAnalysis(
				List.copyOf(runtimes.values()),
				List.copyOf(environmentStatuses),
				List.copyOf(serviceStatuses),
				List.copyOf(portObservations)
		);
	}

	private InstalledRuntime inspectRuntime(String runtime) {
		return switch (runtime) {
			case "java" -> runtimeInspector.inspectJava();
			case "node" -> runtimeInspector.inspectNode();
			case "docker" -> runtimeInspector.inspectDocker();
			default -> InstalledRuntime.unverified(runtime);
		};
	}

	private static void addConfiguredServiceStatuses(
			ProjectAnalysis project,
			List<ServiceStatus> serviceStatuses,
			List<PortObservation> portObservations
	) {
		Set<String> known = new LinkedHashSet<>();
		for (ServiceStatus status : serviceStatuses) {
			known.add(status.serviceName().toLowerCase());
		}
		for (ServiceRequirement requirement : project.serviceRequirements()) {
			if (requirement.composeBacked() || !known.add(requirement.serviceName().toLowerCase())) {
				continue;
			}
			if (requirement.publishedHostPorts().isEmpty()) {
				serviceStatuses.add(ServiceStatus.unverified(requirement.serviceName()));
				continue;
			}
			int port = requirement.publishedHostPorts().getFirst();
			PortObservation observation = null;
			for (PortObservation candidate : portObservations) {
				if (candidate.port() == port) {
					observation = candidate;
					break;
				}
			}
			if (observation == null || observation.verificationState() == VerificationState.UNVERIFIED) {
				serviceStatuses.add(ServiceStatus.unverified(requirement.serviceName()));
			} else if (observation.availability() == PortAvailability.LISTENING) {
				if (occupantMatchesService(requirement, observation.occupantHint())) {
					serviceStatuses.add(ServiceStatus.running(requirement.serviceName()));
				} else {
					// Something else is listening on the configured port; we cannot confirm
					// the configured service itself is what's actually running there.
					serviceStatuses.add(ServiceStatus.unverified(requirement.serviceName()));
				}
			} else {
				serviceStatuses.add(ServiceStatus.stopped(requirement.serviceName()));
			}
		}
	}

	private static boolean occupantMatchesService(ServiceRequirement requirement, String occupantHint) {
		if (occupantHint == null || occupantHint.isBlank()) {
			return false;
		}
		String occupant = occupantHint.toLowerCase(Locale.ROOT);
		String name = requirement.serviceName().toLowerCase(Locale.ROOT);
		if (occupant.contains(name) || name.contains(occupant)) {
			return true;
		}
		String image = requirement.image();
		if (image == null || image.isBlank()) {
			return false;
		}
		String imageName = image.contains(":") ? image.substring(0, image.indexOf(':')) : image;
		imageName = imageName.toLowerCase(Locale.ROOT);
		return !imageName.isBlank() && (occupant.contains(imageName) || imageName.contains(occupant));
	}
}
