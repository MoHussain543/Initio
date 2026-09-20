package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortObservation;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectMetadata;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortConflictRuleTest {

	@Test
	void reportsApplicationPortConflicts() {
		DetectionSource source = new DetectionSource(
				Path.of("application.properties"),
				"server.port",
				DetectionConfidence.HIGH
		);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				List.of(new PortExpectation(8080, PortRole.APPLICATION, "Spring Boot application", source)),
				List.of(),
				List.of()
		);
		AnalysisContext context = new AnalysisContext(
				project,
				new LocalEnvironmentAnalysis(
						List.of(),
						List.of(),
						List.of(),
						List.of(PortObservation.listening(8080, "node"))
				)
		);

		var issues = new PortConflictRule().evaluate(context);

		assertEquals(1, issues.size());
		assertTrue(issues.getFirst().title().contains("8080"));
	}
}
