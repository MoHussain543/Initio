package com.mbh.initio.analysis;

import com.mbh.initio.diagnostic.DiagnosticEngine;
import com.mbh.initio.diagnostic.DiagnosticSuppressionFilter;
import com.mbh.initio.diagnostic.ReadinessCalculator;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.ReadinessScore;
import com.mbh.initio.projectconfig.DiagnosticSuppression;
import com.mbh.initio.projectconfig.InitioProjectConfig;
import com.mbh.initio.system.LocalEnvironmentInspector;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class AnalysisEngine {

	private final ProjectAnalyzer projectAnalyzer;
	private final ProjectAnalysisEnricher projectAnalysisEnricher;
	private final LocalEnvironmentInspector localEnvironmentInspector;
	private final DiagnosticEngine diagnosticEngine;
	private final ReadinessCalculator readinessCalculator;

	public AnalysisEngine(
			ProjectAnalyzer projectAnalyzer,
			LocalEnvironmentInspector localEnvironmentInspector,
			DiagnosticEngine diagnosticEngine,
			ReadinessCalculator readinessCalculator
	) {
		this(
				projectAnalyzer,
				new ProjectAnalysisEnricher(),
				localEnvironmentInspector,
				diagnosticEngine,
				readinessCalculator
		);
	}

	public AnalysisEngine(
			ProjectAnalyzer projectAnalyzer,
			ProjectAnalysisEnricher projectAnalysisEnricher,
			LocalEnvironmentInspector localEnvironmentInspector,
			DiagnosticEngine diagnosticEngine,
			ReadinessCalculator readinessCalculator
	) {
		this.projectAnalyzer = Objects.requireNonNull(projectAnalyzer, "projectAnalyzer");
		this.projectAnalysisEnricher = Objects.requireNonNull(projectAnalysisEnricher, "projectAnalysisEnricher");
		this.localEnvironmentInspector = Objects.requireNonNull(localEnvironmentInspector, "localEnvironmentInspector");
		this.diagnosticEngine = Objects.requireNonNull(diagnosticEngine, "diagnosticEngine");
		this.readinessCalculator = Objects.requireNonNull(readinessCalculator, "readinessCalculator");
	}

	public AnalysisResult run(Path projectPath) {
		var detected = projectAnalyzer.analyze(projectPath);
		var effective = projectAnalysisEnricher.enrich(detected);
		var project = effective.project();
		var local = localEnvironmentInspector.inspect(project);
		var context = new AnalysisContext(project, local);
		List<DiagnosticIssue> allIssues = diagnosticEngine.evaluate(context);
		List<DiagnosticSuppression> suppressions = effective.configOptional()
				.map(InitioProjectConfig::suppressions)
				.orElse(List.of());
		List<DiagnosticIssue> issues = DiagnosticSuppressionFilter.visible(allIssues, suppressions);
		ReadinessScore readiness = readinessCalculator.calculate(context, issues, suppressions);
		return new AnalysisResult(project, local, issues, readiness, effective, allIssues);
	}
}
