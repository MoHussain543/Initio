package com.mbh.initio.cli;

import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.testsupport.InitioCli;
import com.mbh.initio.testsupport.InitioCli.CommandResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
	void doctorReportsDeclarationDriftWarningsForFullstackFixture() {
		CommandResult output = InitioCli.execute("doctor", FixtureRepositories.fullstack().toString());

		assertTrue(output.stdout().contains("[WARN]"));
		assertTrue(output.stdout().contains("Conflicting Node engine declarations"));
	}

	@Test
	void doctorSucceedsWhenNoErrorsAreReported() {
		CommandResult output = InitioCli.execute("doctor", FixtureRepositories.empty().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("No issues found."));
	}

	@Test
	void doctorReportsConfiguredEnvironmentVariablesAsMissing() {
		CommandResult output = InitioCli.execute("doctor", FixtureRepositories.withInitioConfig().toString());

		assertEquals(1, output.exitCode());
		assertTrue(output.stdout().contains("INTERNAL_API_KEY"));
		assertTrue(output.stdout().contains("initio.yml"));
		assertFalse(output.stdout().contains("LEGACY_API_KEY"));
		assertFalse(output.stdout().contains("docker compose up -d redis"));
	}

	@Test
	void doctorOmitsSuppressedEnvAndDeclarationDrift() {
		CommandResult output = InitioCli.execute("doctor", FixtureRepositories.configSuppression().toString());

		assertTrue(output.stdout().contains("JWT_SECRET"));
		assertFalse(output.stdout().contains("LEGACY_API_KEY"));
		assertFalse(output.stdout().contains("Conflicting Node engine declarations"));
	}

	@Test
	void doctorWarnsWhenConfiguredJavaConflictsWithPom() {
		CommandResult output = InitioCli.execute("doctor", FixtureRepositories.configRuntimeConflict().toString());

		assertTrue(output.stdout().contains("[WARN]"));
		assertTrue(output.stdout().contains("Configured Java requirement conflicts with repository declaration"));
		assertTrue(output.stdout().contains("21"));
		assertTrue(output.stdout().contains("25"));
	}

	@Test
	void doctorFailsCleanlyWhenInitioYmlIsInvalid() {
		CommandResult output = InitioCli.execute("doctor", FixtureRepositories.invalidInitioConfig().toString());

		assertEquals(1, output.exitCode());
		assertTrue(output.stderr().contains("Initio could not analyze this project"));
		assertTrue(output.stderr().contains("commands[0].category"));
		assertTrue(output.stderr().contains("initio config validate"));
	}
}
