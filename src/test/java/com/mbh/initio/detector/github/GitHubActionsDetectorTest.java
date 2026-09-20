package com.mbh.initio.detector.github;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubActionsDetectorTest {

	private final GitHubActionsDetector detector = new GitHubActionsDetector();

	@Test
	void detectsGitHubActionsExpectationsFromWorkflows() {
		ProjectContext context = new ProjectContext(FixtureRepositories.springMaven());

		assertTrue(detector.supports(context));
		DetectionResult result = detector.detect(context);

		assertTrue(result.technologies().stream().anyMatch(technology -> technology.name().equals("GitHub Actions")));
		assertEquals(1, result.ciExpectations().size());
		assertEquals("25", result.ciExpectations().getFirst().javaVersions().getFirst());
		assertTrue(result.ciExpectations().getFirst().runCommands().contains("./mvnw test"));
	}
}
