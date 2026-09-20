package com.mbh.initio.analysis;

import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.ProjectAnalysis;
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
		if (environment == detected.environmentVariableRequirements()) {
			return detected;
		}
		return new ProjectAnalysis(
				detected.projectPath(),
				detected.metadata(),
				detected.technologies(),
				detected.runtimeRequirements(),
				environment,
				detected.serviceRequirements(),
				detected.portExpectations(),
				detected.projectCommands(),
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
