package com.mbh.initio.analysis;

import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.projectconfig.InitioProjectConfig;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public record EffectiveProjectAnalysis(
		ProjectAnalysis detected,
		ProjectAnalysis project,
		InitioProjectConfig config,
		Path configFile
) {
	public EffectiveProjectAnalysis {
		Objects.requireNonNull(detected, "detected");
		Objects.requireNonNull(project, "project");
		if (config != null) {
			Objects.requireNonNull(configFile, "configFile");
		}
	}

	public static EffectiveProjectAnalysis withoutConfig(ProjectAnalysis detected) {
		return new EffectiveProjectAnalysis(detected, detected, null, null);
	}

	public static EffectiveProjectAnalysis withConfig(
			ProjectAnalysis detected,
			ProjectAnalysis project,
			InitioProjectConfig config,
			Path configFile
	) {
		return new EffectiveProjectAnalysis(
				detected,
				project,
				Objects.requireNonNull(config, "config"),
				Objects.requireNonNull(configFile, "configFile")
		);
	}

	public Optional<InitioProjectConfig> configOptional() {
		return Optional.ofNullable(config);
	}

	public boolean hasConfig() {
		return config != null;
	}
}
