package com.mbh.initio.cli;

import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.testsupport.InitioCli;
import com.mbh.initio.testsupport.InitioCli.CommandResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckCommandTest {

	@Test
	void helpListsCheckAndInfo() {
		CommandResult output = InitioCli.execute("--help");

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("check"));
		assertTrue(output.stdout().contains("info"));
	}

	@Test
	void checkPrintsDeclaredRequirementsWithoutMachineLookup() {
		CommandResult output = InitioCli.execute("check", FixtureRepositories.springMaven().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("INITIO"));
		assertTrue(output.stdout().contains("Project: Spring Maven Demo"));
		assertTrue(output.stdout().contains("Java"));
		assertTrue(output.stdout().contains("Spring Boot"));
		assertTrue(output.stdout().contains("Maven"));
		assertTrue(output.stdout().contains("Maven Wrapper"));
		assertTrue(output.stdout().contains("Java 25"));
		assertTrue(output.stdout().contains("Runtime"));
		assertTrue(output.stdout().contains("Project readiness:"));
	}

	@Test
	void checkListsSeparateNodeRequirementsForFullstackFixture() {
		CommandResult output = InitioCli.execute("check", FixtureRepositories.fullstack().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("Node >=20"));
		assertTrue(output.stdout().contains("Node ^22"));
	}

	@Test
	void checkListsMissingEnvironmentVariables() {
		CommandResult output = InitioCli.execute("check", FixtureRepositories.brokenEnv().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("Environment"));
		assertTrue(output.stdout().contains("JWT_SECRET"));
		assertTrue(output.stdout().contains("missing"));
	}

	@Test
	void checkReportsAMissingPath() {
		CommandResult output = InitioCli.execute("check", "/this/path/does-not-exist-initio");

		assertEquals(1, output.exitCode());
		assertTrue(output.stderr().contains("does not exist"));
	}
}
