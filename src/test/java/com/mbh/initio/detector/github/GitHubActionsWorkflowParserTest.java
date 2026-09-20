package com.mbh.initio.detector.github;

import com.mbh.initio.detector.DetectionException;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubActionsWorkflowParserTest {

	private final GitHubActionsWorkflowParser parser = new GitHubActionsWorkflowParser();

	@Test
	void extractsJavaVersionAndRunCommandsFromSpringMavenWorkflow() {
		Path workflow = FixtureRepositories.springMaven()
				.resolve(".github/workflows/build.yml");

		GitHubActionsWorkflowParser.ParsedWorkflow parsed = parser.parse(workflow);

		assertEquals("25", parsed.javaVersions().getFirst());
		assertTrue(parsed.runCommands().contains("./mvnw test"));
		assertTrue(parsed.runCommands().contains("./mvnw package"));
	}

	@Test
	void resolvesSimpleJavaMatrixExpressions() {
		Path workflow = FixtureRepositories.githubMatrix().resolve(".github/workflows/ci.yml");

		GitHubActionsWorkflowParser.ParsedWorkflow parsed = parser.parse(workflow);

		assertTrue(parsed.javaVersions().contains("21"));
		assertTrue(parsed.javaVersions().contains("25"));
		assertFalse(parsed.javaVersions().stream().anyMatch(version -> version.contains("${{")));
	}

	@Test
	void doesNotTreatUnresolvedExpressionsAsVersions() {
		Path workflow = FixtureRepositories.githubUnresolved().resolve(".github/workflows/ci.yml");

		GitHubActionsWorkflowParser.ParsedWorkflow parsed = parser.parse(workflow);

		assertTrue(parsed.javaVersions().isEmpty());
		assertFalse(parsed.javaVersions().contains("${{ needs.setup.outputs.java }}"));
	}

	@Test
	void malformedWorkflowYamlThrowsControlledDetectionException() {
		Path workflow = FixtureRepositories.malformedGitHubActionsYaml()
				.resolve(".github/workflows/ci.yml");

		DetectionException exception = assertThrows(DetectionException.class, () -> parser.parse(workflow));

		assertTrue(exception.getMessage().contains("Could not parse ci.yml"));
		assertTrue(exception.getMessage().contains("Invalid YAML"));
	}
}
