package com.mbh.initio.detector.docker;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockerComposeDetectorTest {

	private final DockerComposeDetector detector = new DockerComposeDetector();

	@Test
	void detectsComposeServicesAndExpectedPorts() {
		ProjectContext context = new ProjectContext(FixtureRepositories.dockerized());

		assertTrue(detector.supports(context));
		DetectionResult result = detector.detect(context);

		assertEquals(2, result.serviceRequirements().size());
		assertTrue(result.technologies().stream().anyMatch(technology -> technology.name().equals("Docker Compose")));
		assertEquals(2, result.portExpectations().size());
		assertTrue(result.portExpectations().stream().allMatch(expectation -> expectation.role() == PortRole.EXPECTED_SERVICE));
		assertTrue(result.portExpectations().stream().anyMatch(expectation -> expectation.port() == 5432));
	}
}
