package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.ServiceRequirement;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockerUnavailableRuleTest {

	@Test
	void reportsDockerMissingWithComposeFileAsSourceReference() {
		DetectionSource source = new DetectionSource(Path.of("docker-compose.yml"), "Compose", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				List.of(),
				List.of(),
				List.of(new ServiceRequirement("postgres", Path.of("docker-compose.yml"), "postgres:16", List.of(5432), source)),
				List.of(),
				List.of(),
				List.of()
		);
		AnalysisContext context = new AnalysisContext(
				project,
				new LocalEnvironmentAnalysis(
						List.of(InstalledRuntime.missing("docker")),
						List.of(),
						List.of(),
						List.of()
				)
		);

		List<DiagnosticIssue> issues = new DockerUnavailableRule().evaluate(context);

		assertEquals(1, issues.size());
		DiagnosticIssue issue = issues.getFirst();
		assertTrue(issue.title().contains("Docker is not available"));
		assertEquals(Path.of("docker-compose.yml"), issue.sourceFile());
	}
}
