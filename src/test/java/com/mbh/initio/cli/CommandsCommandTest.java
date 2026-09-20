package com.mbh.initio.cli;

import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.testsupport.InitioCli;
import com.mbh.initio.testsupport.InitioCli.CommandResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandsCommandTest {

	@Test
	void helpListsCommandsSubcommand() {
		CommandResult output = InitioCli.execute("--help");

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("commands"));
	}

	@Test
	void commandsListsMavenAndNpmCommandsForFullstackFixture() {
		CommandResult output = InitioCli.execute("commands", FixtureRepositories.fullstack().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("INITIO"));
		assertTrue(output.stdout().contains("mvn test"));
		assertTrue(output.stdout().contains("Suggested (Maven convention)"));
	}

	@Test
	void commandsListsNpmScriptsForNodeViteFixture() {
		CommandResult output = InitioCli.execute("commands", FixtureRepositories.nodeVite().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("npm run dev"));
		assertTrue(output.stdout().contains("Declared"));
	}

	@Test
	void commandsListsMakefileTargets() {
		CommandResult output = InitioCli.execute("commands", FixtureRepositories.makeTargets().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("make test"));
		assertTrue(output.stdout().contains("Inferred"));
	}
}
