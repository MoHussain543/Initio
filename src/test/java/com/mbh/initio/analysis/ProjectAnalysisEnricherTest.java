package com.mbh.initio.analysis;

import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.projectconfig.InitioConfigException;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectAnalysisEnricherTest {

	private final ProjectAnalyzer analyzer = ProjectAnalyzers.create();
	private final ProjectAnalysisEnricher enricher = new ProjectAnalysisEnricher();

	@Test
	void missingConfigLeavesDetectedAnalysisUnchanged() {
		ProjectAnalysis detected = analyzer.analyze(FixtureRepositories.springMaven());

		EffectiveProjectAnalysis effective = enricher.enrich(detected);

		assertFalse(effective.hasConfig());
		assertSame(detected, effective.detected());
		assertSame(detected, effective.project());
	}

	@Test
	void validConfigDoesNotMutateDetectedAnalysis() {
		ProjectAnalysis detected = analyzer.analyze(FixtureRepositories.withInitioConfig());

		EffectiveProjectAnalysis effective = enricher.enrich(detected);

		assertTrue(effective.hasConfig());
		assertSame(detected, effective.detected());
		assertEquals(detected.technologies(), effective.detected().technologies());
		assertEquals(detected.runtimeRequirements(), effective.project().runtimeRequirements());
		assertEquals("INTERNAL_API_KEY", effective.config().requiredEnvironmentVariables().getFirst());
	}

	@Test
	void invalidConfigFailsWithControlledMessage() {
		ProjectAnalysis detected = analyzer.analyze(FixtureRepositories.invalidInitioConfig());

		InitioConfigException exception = assertThrows(
				InitioConfigException.class,
				() -> enricher.enrich(detected)
		);
		assertTrue(exception.getMessage().contains("Initio could not analyze this project"));
		assertTrue(exception.getMessage().contains("commands[0].category"));
		assertTrue(exception.getMessage().contains("initio config validate"));
	}
}
