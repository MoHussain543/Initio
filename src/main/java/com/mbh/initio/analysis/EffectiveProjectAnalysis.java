package com.mbh.initio.analysis;

import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.projectconfig.InitioProjectConfig;

import java.util.Objects;
import java.util.Optional;

public record EffectiveProjectAnalysis(
		ProjectAnalysis detected,
		ProjectAnalysis project,
		InitioProjectConfig config
) {
	public EffectiveProjectAnalysis {
		Objects.requireNonNull(detected, "detected");
		Objects.requireNonNull(project, "project");
	}

	public static EffectiveProjectAnalysis withoutConfig(ProjectAnalysis detected) {
		return new EffectiveProjectAnalysis(detected, detected, null);
	}

	public static EffectiveProjectAnalysis withConfig(ProjectAnalysis detected, ProjectAnalysis project, InitioProjectConfig config) {
		return new EffectiveProjectAnalysis(detected, project, Objects.requireNonNull(config, "config"));
	}

	public Optional<InitioProjectConfig> configOptional() {
		return Optional.ofNullable(config);
	}

	public boolean hasConfig() {
		return config != null;
	}
}
