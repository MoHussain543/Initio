package com.mbh.initio.detector.spring;

import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringConfigDetectorTest {

	private final SpringConfigDetector detector = new SpringConfigDetector();

	@Test
	void detectsApplicationPortFromSpringProperties() {
		ProjectContext context = new ProjectContext(FixtureRepositories.springMaven());

		assertTrue(detector.supports(context));
		var result = detector.detect(context);

		assertEquals(1, result.portExpectations().size());
		assertEquals(8080, result.portExpectations().getFirst().port());
		assertEquals(PortRole.APPLICATION, result.portExpectations().getFirst().role());
	}

	@Test
	void detectsApplicationPortFromApplicationYaml() {
		ProjectContext context = new ProjectContext(FixtureRepositories.springYaml());

		assertTrue(detector.supports(context));
		var result = detector.detect(context);

		assertEquals(9090, result.portExpectations().getFirst().port());
	}
}
