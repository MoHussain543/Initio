package com.mbh.initio.analysis;

import com.mbh.initio.diagnostic.VersionDriftHelper;
import com.mbh.initio.model.CommandOrigin;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectCommand;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.projectconfig.ConfiguredCommand;
import com.mbh.initio.projectconfig.ConfiguredService;
import com.mbh.initio.projectconfig.InitioConfigException;
import com.mbh.initio.projectconfig.InitioConfigLoadResult;
import com.mbh.initio.projectconfig.InitioConfigLoader;
import com.mbh.initio.projectconfig.InitioProjectConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ProjectAnalysisEnricher {

	private final InitioConfigLoader configLoader;

	public ProjectAnalysisEnricher() {
		this(new InitioConfigLoader());
	}

	public ProjectAnalysisEnricher(InitioConfigLoader configLoader) {
		this.configLoader = Objects.requireNonNull(configLoader, "configLoader");
	}

	public EffectiveProjectAnalysis enrich(ProjectAnalysis detected) {
		Objects.requireNonNull(detected, "detected");
		InitioConfigLoadResult loaded = configLoader.load(detected.projectPath());
		if (loaded.isMissing()) {
			return EffectiveProjectAnalysis.withoutConfig(detected);
		}
		if (loaded.isInvalid()) {
			throw InitioConfigException.invalid(loaded);
		}
		InitioProjectConfig config = loaded.config();
		return EffectiveProjectAnalysis.withConfig(detected, apply(detected, config, loaded.file()), config);
	}

	static ProjectAnalysis apply(ProjectAnalysis detected, InitioProjectConfig config, Path configFile) {
		Objects.requireNonNull(detected, "detected");
		Objects.requireNonNull(config, "config");
		Objects.requireNonNull(configFile, "configFile");
		List<EnvironmentVariableRequirement> environment = mergeEnvironment(detected, config, configFile);
		List<ProjectCommand> commands = mergeCommands(detected, config, configFile);
		List<RuntimeRequirement> runtimes = mergeRuntimes(detected, config, configFile);
		MergedServices mergedServices = mergeServices(detected, config, configFile);
		if (environment == detected.environmentVariableRequirements()
				&& commands == detected.projectCommands()
				&& runtimes == detected.runtimeRequirements()
				&& mergedServices.services() == detected.serviceRequirements()
				&& mergedServices.ports() == detected.portExpectations()) {
			return detected;
		}
		return new ProjectAnalysis(
				detected.projectPath(),
				detected.metadata(),
				detected.technologies(),
				runtimes,
				environment,
				mergedServices.services(),
				mergedServices.ports(),
				commands,
				detected.ciExpectations()
		);
	}

	private static List<EnvironmentVariableRequirement> mergeEnvironment(
			ProjectAnalysis detected,
			InitioProjectConfig config,
			Path configFile
	) {
		if (config.requiredEnvironmentVariables().isEmpty()) {
			return detected.environmentVariableRequirements();
		}
		Set<String> seen = new LinkedHashSet<>();
		List<EnvironmentVariableRequirement> merged = new ArrayList<>();
		for (EnvironmentVariableRequirement requirement : detected.environmentVariableRequirements()) {
			seen.add(requirement.name());
			merged.add(requirement);
		}
		DetectionSource source = configuredSource(detected.projectPath(), configFile);
		boolean added = false;
		for (String name : config.requiredEnvironmentVariables()) {
			if (!seen.add(name)) {
				continue;
			}
			merged.add(new EnvironmentVariableRequirement(name, source));
			added = true;
		}
		if (!added) {
			return detected.environmentVariableRequirements();
		}
		return List.copyOf(merged);
	}

	private static List<ProjectCommand> mergeCommands(
			ProjectAnalysis detected,
			InitioProjectConfig config,
			Path configFile
	) {
		if (config.commands().isEmpty()) {
			return detected.projectCommands();
		}
		Set<String> seenCommands = new LinkedHashSet<>();
		List<ProjectCommand> merged = new ArrayList<>();
		for (ProjectCommand command : detected.projectCommands()) {
			seenCommands.add(command.command());
			merged.add(command);
		}
		DetectionSource source = configuredSource(detected.projectPath(), configFile);
		boolean added = false;
		for (ConfiguredCommand configured : config.commands()) {
			if (!seenCommands.add(configured.command())) {
				continue;
			}
			merged.add(new ProjectCommand(
					configured.name(),
					configured.command(),
					configured.category(),
					CommandOrigin.CONFIGURED,
					source
			));
			added = true;
		}
		if (!added) {
			return detected.projectCommands();
		}
		return List.copyOf(merged);
	}

	private static List<RuntimeRequirement> mergeRuntimes(
			ProjectAnalysis detected,
			InitioProjectConfig config,
			Path configFile
	) {
		if (config.runtimes().isEmpty()) {
			return detected.runtimeRequirements();
		}
		List<RuntimeRequirement> merged = new ArrayList<>(detected.runtimeRequirements());
		DetectionSource source = configuredSource(detected.projectPath(), configFile);
		boolean added = false;
		for (var entry : config.runtimes().entrySet()) {
			String runtime = entry.getKey().toLowerCase();
			String version = entry.getValue();
			if (hasMatchingMajor(detected.runtimeRequirements(), runtime, version)) {
				continue;
			}
			merged.add(RuntimeRequirement.declared(runtime, version, source));
			added = true;
		}
		if (!added) {
			return detected.runtimeRequirements();
		}
		return List.copyOf(merged);
	}

	private static boolean hasMatchingMajor(List<RuntimeRequirement> requirements, String runtime, String version) {
		for (RuntimeRequirement requirement : requirements) {
			if (!requirement.runtime().equalsIgnoreCase(runtime)) {
				continue;
			}
			if (requirement.requiredVersion() == null || requirement.requiredVersion().isBlank()) {
				continue;
			}
			if (VersionDriftHelper.referenceMajor(requirement.requiredVersion()).isEmpty()) {
				continue;
			}
			if (VersionDriftHelper.referenceMajor(version).isEmpty()) {
				continue;
			}
			if (VersionDriftHelper.sameMajor(requirement.requiredVersion(), version)) {
				return true;
			}
		}
		return false;
	}

	private static MergedServices mergeServices(
			ProjectAnalysis detected,
			InitioProjectConfig config,
			Path configFile
	) {
		if (config.services().isEmpty()) {
			return new MergedServices(detected.serviceRequirements(), detected.portExpectations());
		}
		Set<String> seenNames = new LinkedHashSet<>();
		for (ServiceRequirement requirement : detected.serviceRequirements()) {
			seenNames.add(requirement.serviceName().toLowerCase());
		}
		Set<Integer> seenPorts = new LinkedHashSet<>();
		for (PortExpectation expectation : detected.portExpectations()) {
			seenPorts.add(expectation.port());
		}
		List<ServiceRequirement> services = new ArrayList<>(detected.serviceRequirements());
		List<PortExpectation> ports = new ArrayList<>(detected.portExpectations());
		DetectionSource source = configuredSource(detected.projectPath(), configFile);
		boolean addedService = false;
		boolean addedPort = false;
		for (ConfiguredService configured : config.services()) {
			if (!seenNames.add(configured.name().toLowerCase())) {
				continue;
			}
			services.add(ServiceRequirement.configured(configured.name(), configured.port(), source));
			addedService = true;
			if (seenPorts.add(configured.port())) {
				ports.add(new PortExpectation(
						configured.port(),
						PortRole.EXPECTED_SERVICE,
						configured.name(),
						source
				));
				addedPort = true;
			}
		}
		if (!addedService) {
			return new MergedServices(detected.serviceRequirements(), detected.portExpectations());
		}
		return new MergedServices(
				List.copyOf(services),
				addedPort ? List.copyOf(ports) : detected.portExpectations()
		);
	}

	private record MergedServices(List<ServiceRequirement> services, List<PortExpectation> ports) {
	}

	private static DetectionSource configuredSource(Path projectPath, Path configFile) {
		Path relative = projectPath.relativize(configFile.toAbsolutePath().normalize());
		Path file = relative.getNameCount() == 0 ? configFile.getFileName() : relative;
		return new DetectionSource(
				file,
				"Configured in " + file,
				DetectionConfidence.HIGH
		);
	}
}
