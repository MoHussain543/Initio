package com.mbh.initio.cli;

import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.testsupport.InitioCli;
import com.mbh.initio.testsupport.InitioCli.CommandResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoctorCommandTest {

	@Test
	void helpListsDoctorCommand() {
		CommandResult output = InitioCli.execute("--help");

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("doctor"));
	}

	@Test
	void doctorNumbersIssuesForBrokenEnvironmentFixture() {
		CommandResult output = InitioCli.execute("doctor", FixtureRepositories.brokenEnv().toString());

		assertEquals(1, output.exitCode());
		assertTrue(output.stdout().contains("INITIO DOCTOR"));
		assertTrue(output.stdout().contains("[ERROR]"));
		assertTrue(output.stdout().contains("JWT_SECRET"));
		assertTrue(output.stdout().contains("1."));
	}

	@Test
	void doctorSucceedsWhenNoErrorsAreReported() {
		CommandResult output = InitioCli.execute("doctor", FixtureRepositories.empty().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("No issues found."));
	}
}
