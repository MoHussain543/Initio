package com.mbh.initio.cli;

import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.testsupport.InitioCli;
import com.mbh.initio.testsupport.InitioCli.CommandResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigValidateCommandTest {

	@Test
	void helpListsConfigCommand() {
		CommandResult output = InitioCli.execute("--help");

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("config"));
	}

	@Test
	void configHelpListsValidateAndNotInit() {
		CommandResult output = InitioCli.execute("config", "--help");

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("validate"));
		assertFalse(output.stdout().contains("config init"));
	}

	@Test
	void validateSucceedsWhenConfigIsMissing() {
		CommandResult output = InitioCli.execute("config", "validate", FixtureRepositories.empty().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("INITIO CONFIG"));
		assertTrue(output.stdout().contains("No initio.yml found"));
		assertTrue(output.stdout().contains("optional"));
	}

	@Test
	void validateReportsCountsForValidConfig() {
		CommandResult output = InitioCli.execute(
				"config",
				"validate",
				FixtureRepositories.withInitioConfig().toString()
		);

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("Configuration valid"));
		assertTrue(output.stdout().contains("initio.yml"));
		assertTrue(output.stdout().contains("environment.required: 2"));
		assertTrue(output.stdout().contains("runtimes: 2"));
		assertTrue(output.stdout().contains("commands: 1"));
		assertTrue(output.stdout().contains("services: 1"));
		assertTrue(output.stdout().contains("ignore: 3"));
	}

	@Test
	void validateReportsFieldErrorsForInvalidConfig() {
		CommandResult output = InitioCli.execute(
				"config",
				"validate",
				FixtureRepositories.invalidInitioConfig().toString()
		);

		assertEquals(1, output.exitCode());
		assertTrue(output.stdout().contains("Invalid initio.yml"));
		assertTrue(output.stdout().contains("commands[0].category"));
		assertTrue(output.stdout().contains("STARTUP"));
		assertTrue(output.stdout().contains("RUN"));
	}

	@Test
	void validateReportsAMissingPath() {
		CommandResult output = InitioCli.execute("config", "validate", "/this/path/does-not-exist-initio");

		assertEquals(1, output.exitCode());
		assertTrue(output.stderr().contains("does not exist"));
	}
}
