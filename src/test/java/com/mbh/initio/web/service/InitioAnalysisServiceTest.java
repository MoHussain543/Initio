package com.mbh.initio.web.service;

import com.mbh.initio.analysis.AnalysisEngines;
import com.mbh.initio.config.InitioDashboardProperties;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InitioAnalysisServiceTest {

	@Test
	void analyzeUsesConfiguredProjectPath() {
		var projectPath = FixtureRepositories.springMaven();
		InitioDashboardProperties properties = new InitioDashboardProperties();
		properties.setProjectPath(projectPath.toString());
		InitioAnalysisService service = new InitioAnalysisService(AnalysisEngines.createDefault(), properties);

		assertEquals("Spring Maven Demo", service.analyze().project().metadata().name());
	}
}
