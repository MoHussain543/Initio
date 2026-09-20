package com.mbh.initio.web.mapper;

import com.mbh.initio.analysis.AnalysisEngines;
import com.mbh.initio.analysis.AnalysisResult;
import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.web.dto.EnvironmentRowResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalysisResponseMapperTest {

	private final AnalysisResponseMapper mapper = new AnalysisResponseMapper();

	@Test
	void mapsSpringMavenFixtureWithCommandsAndReadiness() {
		AnalysisResult result = AnalysisEngines.createDefault().run(FixtureRepositories.springMaven());

		var response = mapper.toResponse(result);

		assertEquals("Spring Maven Demo", response.project().name());
		assertTrue(response.readiness().score() == null || response.readiness().score() >= 0);
		assertFalse(response.commands().isEmpty());
	}

	@Test
	void environmentRowsExposeNamesAndStatusOnly() {
		AnalysisResult result = AnalysisEngines.createDefault().run(FixtureRepositories.brokenEnv());

		var response = mapper.toResponse(result);

		assertFalse(response.environment().isEmpty());
		for (EnvironmentRowResponse row : response.environment()) {
			assertFalse(row.name().isBlank());
			assertFalse(row.status().isBlank());
		}
		assertTrue(response.issues().stream().anyMatch(issue ->
				"MISSING_ENVIRONMENT_VARIABLE".equals(issue.ruleId())
						&& issue.sources().contains("JWT_SECRET")
						&& issue.suggestedAction() == null
		));
	}

	@Test
	void configuredEnvironmentRowsIncludeInitioYmlSource() {
		AnalysisResult result = AnalysisEngines.createDefault().run(FixtureRepositories.withInitioConfig());

		var response = mapper.toResponse(result);
		assertTrue(response.environment().stream().anyMatch(row ->
				"INTERNAL_API_KEY".equals(row.name()) && row.source().contains("initio.yml")));
		assertTrue(response.environment().stream().noneMatch(row -> row.name() == null));
	}

	@Test
	void configuredCommandsHaveConfiguredOrigin() {
		AnalysisResult result = AnalysisEngines.createDefault().run(FixtureRepositories.withInitioConfig());

		var response = mapper.toResponse(result);
		assertTrue(response.commands().stream().anyMatch(command ->
				"./scripts/integration-test.sh".equals(command.command())
						&& "CONFIGURED".equals(command.origin())
						&& command.sourceFile().contains("initio.yml")
		));
	}

	@Test
	void configuredServiceHintsAppearInServicesAndPorts() {
		AnalysisResult result = AnalysisEngines.createDefault().run(FixtureRepositories.withInitioConfig());

		var response = mapper.toResponse(result);
		assertTrue(response.services().stream().anyMatch(service ->
				"redis".equals(service.name()) && service.source().contains("initio.yml")));
		assertTrue(response.ports().stream().anyMatch(port ->
				port.port() == 6379 && port.source().contains("initio.yml")));
	}

	@Test
	void mapsProjectConfigurationTeaserFromInitioYml() {
		AnalysisResult result = AnalysisEngines.createDefault().run(FixtureRepositories.withInitioConfig());

		var configuration = mapper.toResponse(result).configuration();

		assertTrue(configuration.present());
		assertEquals("initio.yml", configuration.file());
		assertEquals(2, configuration.environmentRequired());
		assertEquals(2, configuration.runtimes());
		assertEquals(1, configuration.commands());
		assertEquals(1, configuration.services());
		assertEquals(3, configuration.ignore());
	}

	@Test
	void absentConfigurationWhenProjectHasNoInitioYml() {
		AnalysisResult result = AnalysisEngines.createDefault().run(FixtureRepositories.springMaven());

		var configuration = mapper.toResponse(result).configuration();

		assertFalse(configuration.present());
		assertEquals(null, configuration.file());
		assertEquals(0, configuration.environmentRequired());
	}

	@Test
	void runtimeConflictKeepsDetectedAndConfiguredSources() {
		AnalysisResult result = AnalysisEngines.createDefault().run(FixtureRepositories.configRuntimeConflict());

		var response = mapper.toResponse(result);

		assertTrue(response.runtime().stream().anyMatch(row ->
				"java".equals(row.runtime())
						&& "21".equals(row.requiredVersion())
						&& row.source().contains("pom.xml")));
		assertTrue(response.runtime().stream().anyMatch(row ->
				"java".equals(row.runtime())
						&& "25".equals(row.requiredVersion())
						&& row.source().contains("initio.yml")));
	}
}
