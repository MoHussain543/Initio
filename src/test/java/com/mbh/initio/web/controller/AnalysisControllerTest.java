package com.mbh.initio.web.controller;

import com.mbh.initio.analysis.AnalysisEngines;
import com.mbh.initio.analysis.AnalysisResult;
import com.mbh.initio.config.InitioDashboardProperties;
import com.mbh.initio.detector.DetectionException;
import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.web.advice.InitioApiExceptionHandler;
import com.mbh.initio.web.mapper.AnalysisResponseMapper;
import com.mbh.initio.web.service.InitioAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalysisControllerTest {

	@Test
	void analysisReturnsProjectSnapshot() throws Exception {
		MockMvc mockMvc = mockMvcFor(FixtureRepositories.springMaven());

		mockMvc.perform(get("/api/v1/analysis"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.project.name").value("Spring Maven Demo"))
				.andExpect(jsonPath("$.readiness.score").isNumber())
				.andExpect(jsonPath("$.runtime").isArray())
				.andExpect(jsonPath("$.commands").isArray());
	}

	@Test
	void analysisJsonDoesNotExposeEnvironmentValues() throws Exception {
		MockMvc mockMvc = mockMvcFor(FixtureRepositories.brokenEnv());

		String body = mockMvc.perform(get("/api/v1/analysis"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.environment", hasSize(greaterThan(0))))
				.andReturn()
				.getResponse()
				.getContentAsString();

		org.junit.jupiter.api.Assertions.assertFalse(body.contains("\"value\""));
	}

	@Test
	void detectionFailureReturnsStructuredError() throws Exception {
		InitioAnalysisService failingService = new InitioAnalysisService(
				AnalysisEngines.createDefault(),
				propertiesFor(FixtureRepositories.springMaven())
		) {
			@Override
			public AnalysisResult analyze() {
				throw new DetectionException("Could not parse pom.xml");
			}
		};
		MockMvc mockMvc = mockMvcFor(failingService);

		mockMvc.perform(get("/api/v1/analysis"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("PROJECT_ANALYSIS_FAILED"))
				.andExpect(jsonPath("$.message", containsString("Could not parse")));
	}

	private static MockMvc mockMvcFor(java.nio.file.Path projectPath) {
		InitioDashboardProperties properties = propertiesFor(projectPath);
		InitioAnalysisService service = new InitioAnalysisService(AnalysisEngines.createDefault(), properties);
		return mockMvcFor(service);
	}

	private static MockMvc mockMvcFor(InitioAnalysisService service) {
		AnalysisController controller = new AnalysisController(service, new AnalysisResponseMapper());
		return MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new InitioApiExceptionHandler())
				.build();
	}

	private static InitioDashboardProperties propertiesFor(java.nio.file.Path projectPath) {
		InitioDashboardProperties properties = new InitioDashboardProperties();
		properties.setProjectPath(projectPath.toString());
		return properties;
	}
}
