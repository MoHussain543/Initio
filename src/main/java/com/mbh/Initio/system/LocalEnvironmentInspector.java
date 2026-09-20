package com.mbh.initio.system;

import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.EnvironmentVariableStatus;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.ServiceStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class LocalEnvironmentInspector {

	private final RuntimeInspector runtimeInspector;
	private final DockerInspector dockerInspector;

	public LocalEnvironmentInspector(RuntimeInspector runtimeInspector, DockerInspector dockerInspector) {
		this.runtimeInspector = Objects.requireNonNull(runtimeInspector, "runtimeInspector");
		this.dockerInspector = Objects.requireNonNull(dockerInspector, "dockerInspector");
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
		if (!project.serviceRequirements().isEmpty() && !runtimes.containsKey("docker")) {
			runtimes.put("docker", inspectRuntime("docker"));
		}

		EnvironmentProvider environmentProvider = new DefaultEnvironmentProvider(project.projectPath());
		List<EnvironmentVariableStatus> environmentStatuses = new ArrayList<>();
		for (EnvironmentVariableRequirement requirement : project.environmentVariableRequirements()) {
			environmentStatuses.add(environmentProvider.inspect(requirement.name()));
		}

		List<ServiceStatus> serviceStatuses = dockerInspector.inspectServices(project);

		return new LocalEnvironmentAnalysis(
				List.copyOf(runtimes.values()),
				List.copyOf(environmentStatuses),
				List.copyOf(serviceStatuses)
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
}
