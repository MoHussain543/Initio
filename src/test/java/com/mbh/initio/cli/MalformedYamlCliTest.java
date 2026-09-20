package com.mbh.initio.cli;

import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.testsupport.InitioCli;
import com.mbh.initio.testsupport.InitioCli.CommandResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MalformedYamlCliTest {

	@ParameterizedTest
	@MethodSource("malformedRepositories")
	void infoFailsWithControlledParseError(Path projectPath, String fileName) {
		assertControlledFailure(InitioCli.execute("info", projectPath.toString()), fileName);
	}

	@ParameterizedTest
	@MethodSource("malformedRepositories")
	void checkFailsWithControlledParseError(Path projectPath, String fileName) {
		assertControlledFailure(InitioCli.execute("check", projectPath.toString()), fileName);
	}

	private static Stream<Arguments> malformedRepositories() {
		return Stream.of(
				Arguments.of(FixtureRepositories.malformedCompose(), "docker-compose.yml"),
				Arguments.of(FixtureRepositories.malformedSpringYaml(), "application.yml"),
				Arguments.of(FixtureRepositories.malformedGitHubActionsYaml(), "ci.yml")
		);
	}

	private static void assertControlledFailure(CommandResult output, String fileName) {
		assertEquals(1, output.exitCode());
		String stderr = output.stderr();
		assertTrue(stderr.contains("Initio could not analyze this project"));
		assertTrue(stderr.contains("Could not parse " + fileName));
		assertTrue(stderr.contains("Invalid YAML"));
		assertFalse(stderr.contains("Exception"));
		assertFalse(stderr.contains("org.yaml.snakeyaml"));
		assertFalse(stderr.contains("\tat "));
		assertFalse(output.stdout().contains("org.yaml.snakeyaml"));
	}
}
