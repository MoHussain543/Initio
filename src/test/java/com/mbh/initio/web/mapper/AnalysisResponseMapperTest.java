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
		assertTrue(response.readiness().score() >= 0);
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
}
