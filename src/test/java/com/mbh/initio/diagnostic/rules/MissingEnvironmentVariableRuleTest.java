package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.EnvironmentVariableStatus;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.PresenceStatus;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectMetadata;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MissingEnvironmentVariableRuleTest {

	@Test
	void reportsMissingEnvironmentVariables() {
		DetectionSource source = new DetectionSource(Path.of(".env.example"), "Expected", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				List.of(),
				List.of(new EnvironmentVariableRequirement("JWT_SECRET", source)),
				List.of(),
				List.of(),
				List.of(),
				List.of()
		);
		AnalysisContext context = new AnalysisContext(
				project,
				new LocalEnvironmentAnalysis(
						List.of(),
						List.of(EnvironmentVariableStatus.verified("JWT_SECRET", PresenceStatus.MISSING)),
						List.of(),
						List.of()
				)
		);

		var issues = new MissingEnvironmentVariableRule().evaluate(context);

		assertEquals(1, issues.size());
		assertEquals(com.mbh.initio.diagnostic.DiagnosticRuleId.MISSING_ENVIRONMENT_VARIABLE, issues.getFirst().ruleId());
		assertEquals("JWT_SECRET", issues.getFirst().environmentName());
		assertTrue(issues.getFirst().title().contains("JWT_SECRET is missing"));
	}
}
