package com.mbh.initio.web;

import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DashboardIntegrationTest {

	@LocalServerPort
	private int port;

	@DynamicPropertySource
	static void dashboardProjectPath(DynamicPropertyRegistry registry) {
		registry.add("initio.dashboard.project-path", () -> FixtureRepositories.springMaven().toString());
	}

	@Test
	void servesIndexAndAnalysisApi() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		String base = "http://127.0.0.1:" + port;

		HttpResponse<String> index = client.send(
				HttpRequest.newBuilder(URI.create(base + "/")).GET().build(),
				HttpResponse.BodyHandlers.ofString()
		);
		assertEquals(200, index.statusCode());
		assertTrue(index.body().contains("Initio"));
		assertTrue(index.body().contains("dashboard-main"));
		assertTrue(index.body().contains("Project configuration"));

		HttpResponse<String> analysis = client.send(
				HttpRequest.newBuilder(URI.create(base + "/api/v1/analysis")).GET().build(),
				HttpResponse.BodyHandlers.ofString()
		);
		assertEquals(200, analysis.statusCode());
		assertTrue(analysis.body().contains("\"name\":\"Spring Maven Demo\""));
		assertTrue(analysis.body().contains("\"configuration\""));
		assertTrue(analysis.body().contains("\"present\":false"));
		assertFalse(analysis.body().contains("\"value\""));
	}
}
