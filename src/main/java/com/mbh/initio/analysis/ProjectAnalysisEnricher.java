package com.mbh.initio.analysis;

import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.projectconfig.InitioConfigException;
import com.mbh.initio.projectconfig.InitioConfigLoadResult;
import com.mbh.initio.projectconfig.InitioConfigLoader;
import com.mbh.initio.projectconfig.InitioProjectConfig;

import java.util.Objects;

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
		return EffectiveProjectAnalysis.withConfig(detected, apply(detected, config), config);
	}

	static ProjectAnalysis apply(ProjectAnalysis detected, InitioProjectConfig config) {
		Objects.requireNonNull(detected, "detected");
		Objects.requireNonNull(config, "config");
		return detected;
	}
}
