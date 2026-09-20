package com.mbh.initio.detector.environment;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentDetectorTest {

	private final EnvironmentDetector detector = new EnvironmentDetector();

	@Test
	void supportsRepositoriesWithEnvExample() {
		assertTrue(detector.supports(new ProjectContext(FixtureRepositories.brokenEnv())));
		assertFalse(detector.supports(new ProjectContext(FixtureRepositories.empty())));
	}

	@Test
	void detectsExpectedVariableNamesOnly() {
		DetectionResult result = detector.detect(new ProjectContext(FixtureRepositories.brokenEnv()));

		assertEquals(2, result.environmentVariableRequirements().size());
		assertEquals("DATABASE_URL", result.environmentVariableRequirements().get(0).name());
		assertEquals("JWT_SECRET", result.environmentVariableRequirements().get(1).name());
	}
}
