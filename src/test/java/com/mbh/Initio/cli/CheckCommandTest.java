package com.mbh.Initio.cli;

import com.mbh.Initio.testsupport.FixtureRepositories;
import com.mbh.Initio.testsupport.InitioCli;
import com.mbh.Initio.testsupport.InitioCli.CommandResult;
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
		assertTrue(output.stdout().contains("Local environment: not checked"));
	}

	@Test
	void checkReportsAMissingPath() {
		CommandResult output = InitioCli.execute("check", "/this/path/does-not-exist-initio");

		assertEquals(1, output.exitCode());
		assertTrue(output.stderr().contains("does not exist"));
	}
}
