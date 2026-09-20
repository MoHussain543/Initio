package com.mbh.initio.cli;

import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.testsupport.InitioCli;
import com.mbh.initio.testsupport.InitioCli.CommandResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardCommandTest {

	@Test
	void rootHelpListsDashboard() {
		CommandResult output = InitioCli.execute("--help");

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("dashboard"));
	}

	@Test
	void dashboardHelpShowsHostAndPortDefaults() {
		CommandResult output = InitioCli.execute("dashboard", "--help");

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("--port"));
		assertTrue(output.stdout().contains("7331"));
		assertTrue(output.stdout().contains("--host"));
	}

	@Test
	void dashboardRejectsMissingProjectPath() {
		CommandResult output = InitioCli.execute("dashboard", "/path/does-not-exist-initio-dashboard");

		assertEquals(1, output.exitCode());
		assertTrue(output.stderr().contains("does not exist"));
	}

	@Test
	void dashboardRejectsInvalidPort() {
		CommandResult output = InitioCli.execute(
				"dashboard",
				"--port",
				"70000",
				FixtureRepositories.springMaven().toString()
		);

		assertEquals(1, output.exitCode());
		assertTrue(output.stderr().contains("Initio could not start the dashboard"));
		assertTrue(output.stderr().contains("not valid"));
	}

	@Test
	void dashboardReportsPortAlreadyInUse() throws IOException {
		try (ServerSocket occupied = new ServerSocket(0)) {
			int port = occupied.getLocalPort();
			CommandResult output = InitioCli.execute(
					"dashboard",
					"--port",
					String.valueOf(port),
					FixtureRepositories.springMaven().toString()
			);

			assertEquals(1, output.exitCode());
			assertTrue(output.stderr().contains("Initio could not start the dashboard"));
			assertTrue(output.stderr().contains("already in use"));
			assertTrue(output.stderr().contains("initio dashboard --port"));
		}
	}

}
