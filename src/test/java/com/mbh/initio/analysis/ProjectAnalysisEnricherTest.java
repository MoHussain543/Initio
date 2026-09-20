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
	void configuredEnvironmentRequirementsAreAddedWithInitioYmlSource() {
		ProjectAnalysis detected = analyzer.analyze(FixtureRepositories.withInitioConfig());

		EffectiveProjectAnalysis effective = enricher.enrich(detected);

		assertTrue(detected.environmentVariableRequirements().stream()
				.anyMatch(requirement -> requirement.name().equals("DATABASE_URL")));
		assertTrue(detected.environmentVariableRequirements().stream()
				.noneMatch(requirement -> requirement.name().equals("INTERNAL_API_KEY")));
		assertTrue(effective.project().environmentVariableRequirements().stream()
				.anyMatch(requirement -> requirement.name().equals("DATABASE_URL")
						&& requirement.source().file().toString().contains(".env.example")));
		assertTrue(effective.project().environmentVariableRequirements().stream()
				.anyMatch(requirement -> requirement.name().equals("INTERNAL_API_KEY")
						&& requirement.source().file().toString().contains("initio.yml")));
		assertTrue(effective.project().environmentVariableRequirements().stream()
				.anyMatch(requirement -> requirement.name().equals("STRIPE_SECRET_KEY")));
	}

	@Test
	void configuredCommandsAreAddedWithConfiguredOrigin() {
		ProjectAnalysis detected = analyzer.analyze(FixtureRepositories.withInitioConfig());

		EffectiveProjectAnalysis effective = enricher.enrich(detected);

		assertTrue(detected.projectCommands().isEmpty());
		assertTrue(effective.project().projectCommands().stream().anyMatch(command ->
				command.command().equals("./scripts/integration-test.sh")
						&& command.origin() == com.mbh.initio.model.CommandOrigin.CONFIGURED
						&& command.source().file().toString().contains("initio.yml")
		));
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
