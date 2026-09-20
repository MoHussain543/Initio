package com.mbh.initio.analysis;

import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ReadinessScore;

import java.util.List;
import java.util.Objects;

public record AnalysisResult(
		ProjectAnalysis project,
		LocalEnvironmentAnalysis local,
		List<DiagnosticIssue> issues,
		ReadinessScore readiness,
		EffectiveProjectAnalysis effective
) {
	public AnalysisResult {
		Objects.requireNonNull(project, "project");
		Objects.requireNonNull(local, "local");
		issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
		Objects.requireNonNull(readiness, "readiness");
		Objects.requireNonNull(effective, "effective");
	}
}
