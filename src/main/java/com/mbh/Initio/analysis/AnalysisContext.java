package com.mbh.initio.analysis;

import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.ProjectAnalysis;

import java.util.Objects;

public record AnalysisContext(ProjectAnalysis project, LocalEnvironmentAnalysis local) {
	public AnalysisContext {
		Objects.requireNonNull(project, "project");
		Objects.requireNonNull(local, "local");
	}
}
