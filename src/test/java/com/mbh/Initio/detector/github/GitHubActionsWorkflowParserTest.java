package com.mbh.initio.detector.github;

import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
