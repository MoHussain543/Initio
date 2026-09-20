package com.mbh.initio.analysis;

import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.projectconfig.InitioConfigException;
import com.mbh.initio.projectconfig.InitioProjectConfig;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
		assertEquals(detected.runtimeRequirements(), effective.detected().runtimeRequirements());
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
	void configuredRuntimesAreAddedWhenDetectionHasNone() {
		ProjectAnalysis detected = analyzer.analyze(FixtureRepositories.withInitioConfig());

		EffectiveProjectAnalysis effective = enricher.enrich(detected);

		assertTrue(detected.runtimeRequirements().isEmpty());
		assertTrue(effective.project().runtimeRequirements().stream().anyMatch(requirement ->
				requirement.runtime().equals("java")
						&& "25".equals(requirement.requiredVersion())
						&& requirement.source().file().toString().contains("initio.yml")
		));
		assertTrue(effective.project().runtimeRequirements().stream().anyMatch(requirement ->
				requirement.runtime().equals("node")
						&& ">=22".equals(requirement.requiredVersion())
						&& requirement.source().file().toString().contains("initio.yml")
		));
	}

	@Test
	void keepsDetectedAndConfiguredJavaRequirementsWhenMajorsDiffer() {
		ProjectAnalysis detected = analyzer.analyze(FixtureRepositories.configRuntimeConflict());

		EffectiveProjectAnalysis effective = enricher.enrich(detected);

		assertSame(detected, effective.detected());
		assertEquals(1, detected.runtimeRequirements().size());
		assertEquals("21", detected.runtimeRequirements().getFirst().requiredVersion());
		List<RuntimeRequirement> runtimes = effective.project().runtimeRequirements();
		assertEquals(2, runtimes.size());
		assertTrue(runtimes.stream().anyMatch(requirement ->
				requirement.runtime().equals("java")
						&& "21".equals(requirement.requiredVersion())
						&& requirement.source().file().toString().contains("pom.xml")
		));
		assertTrue(runtimes.stream().anyMatch(requirement ->
				requirement.runtime().equals("java")
						&& "25".equals(requirement.requiredVersion())
						&& requirement.source().file().toString().contains("initio.yml")
		));
	}

	@Test
	void doesNotDuplicateRuntimeWhenConfiguredMajorMatchesDetected() {
		ProjectAnalysis detected = analyzer.analyze(FixtureRepositories.mavenPlain());
		InitioProjectConfig config = new InitioProjectConfig(
				List.of(),
				Map.of("java", "21"),
				List.of(),
				List.of(),
				List.of()
		);

		ProjectAnalysis project = ProjectAnalysisEnricher.apply(
				detected,
				config,
				detected.projectPath().resolve("initio.yml")
		);

		assertEquals(1, project.runtimeRequirements().size());
		assertEquals("21", project.runtimeRequirements().getFirst().requiredVersion());
		assertTrue(project.runtimeRequirements().getFirst().source().file().toString().contains("pom.xml"));
	}

	@Test
	void configuredServicesAreAddedAsNameAndPortHints() {
		ProjectAnalysis detected = analyzer.analyze(FixtureRepositories.withInitioConfig());

		EffectiveProjectAnalysis effective = enricher.enrich(detected);

		assertTrue(detected.serviceRequirements().isEmpty());
		assertTrue(detected.portExpectations().isEmpty());
		assertTrue(effective.project().serviceRequirements().stream().anyMatch(requirement ->
				requirement.serviceName().equals("redis")
						&& requirement.publishedHostPorts().equals(List.of(6379))
						&& !requirement.composeBacked()
						&& requirement.image() == null
						&& requirement.source().file().toString().contains("initio.yml")
		));
		assertTrue(effective.project().portExpectations().stream().anyMatch(expectation ->
				expectation.port() == 6379
						&& expectation.role() == com.mbh.initio.model.PortRole.EXPECTED_SERVICE
						&& expectation.label().equals("redis")
						&& expectation.source().file().toString().contains("initio.yml")
		));
	}

	@Test
	void doesNotReplaceComposeServicesWithConfiguredHints() {
		ProjectAnalysis detected = analyzer.analyze(FixtureRepositories.dockerized());
		InitioProjectConfig config = new InitioProjectConfig(
				List.of(),
				Map.of(),
				List.of(),
				List.of(new com.mbh.initio.projectconfig.ConfiguredService("redis", 6379)),
				List.of()
		);

		ProjectAnalysis project = ProjectAnalysisEnricher.apply(
				detected,
				config,
				detected.projectPath().resolve("initio.yml")
		);

		assertEquals(detected.serviceRequirements(), project.serviceRequirements());
		assertTrue(project.serviceRequirements().stream().allMatch(com.mbh.initio.model.ServiceRequirement::composeBacked));
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
