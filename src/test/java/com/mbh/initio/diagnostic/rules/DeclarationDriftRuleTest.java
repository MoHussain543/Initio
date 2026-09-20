package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.analysis.ProjectAnalysisEnricher;
import com.mbh.initio.analysis.ProjectAnalyzers;
import com.mbh.initio.analysis.ProjectAnalyzer;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DeclarationDriftRuleTest {

	private final ProjectAnalyzer projectAnalyzer = ProjectAnalyzers.create();
	private final ProjectAnalysisEnricher enricher = new ProjectAnalysisEnricher();

	@Test
	void warnsWhenMultipleNodeEngineDeclarationsDisagree() {
		AnalysisContext context = new AnalysisContext(
				projectAnalyzer.analyze(FixtureRepositories.fullstack()),
				new LocalEnvironmentAnalysis(List.of(), List.of(), List.of(), List.of())
		);

		List<DiagnosticIssue> issues = new DeclarationDriftRule().evaluate(context);

		assertTrue(issues.stream().anyMatch(issue ->
				issue.severity() == DiagnosticSeverity.WARNING
						&& issue.ruleId() == com.mbh.initio.diagnostic.DiagnosticRuleId.DECLARATION_DRIFT
						&& issue.title().contains("Conflicting Node engine declarations")
		));
	}

	@Test
	void warnsWhenPomJavaVersionDiffersFromCi() {
		AnalysisContext context = new AnalysisContext(
				projectAnalyzer.analyze(FixtureRepositories.ciJavaDrift()),
				new LocalEnvironmentAnalysis(List.of(), List.of(), List.of(), List.of())
		);

		List<DiagnosticIssue> issues = new DeclarationDriftRule().evaluate(context);

		assertTrue(issues.stream().anyMatch(issue ->
				issue.title().contains("Conflicting Java versions")
		));
	}

	@Test
	void warnsWhenConfiguredJavaConflictsWithRepositoryDeclaration() {
		AnalysisContext context = new AnalysisContext(
				enricher.enrich(projectAnalyzer.analyze(FixtureRepositories.configRuntimeConflict())).project(),
				new LocalEnvironmentAnalysis(List.of(), List.of(), List.of(), List.of())
		);

		List<DiagnosticIssue> issues = new DeclarationDriftRule().evaluate(context);

		assertTrue(issues.stream().anyMatch(issue ->
				issue.severity() == DiagnosticSeverity.WARNING
						&& issue.title().contains("Configured Java requirement conflicts with repository declaration")
						&& issue.detail().contains("21")
						&& issue.detail().contains("25")
						&& issue.detail().contains("pom.xml")
						&& issue.detail().contains("initio.yml")
		));
	}
}
