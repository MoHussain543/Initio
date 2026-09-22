package com.mbh.initio.cli;

import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.testsupport.InitioCli;
import com.mbh.initio.testsupport.InitioCli.CommandResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TasksCommandTest {

	@Test
	void helpListsTasksSubcommand() {
		CommandResult output = InitioCli.execute("--help");

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("tasks"));
	}

	@Test
	void tasksListsMavenAndNpmCommandsForFullstackFixture() {
		CommandResult output = InitioCli.execute("tasks", FixtureRepositories.fullstack().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("INITIO"));
		assertTrue(output.stdout().contains("mvn test"));
		assertTrue(output.stdout().contains("Suggested (Maven convention)"));
		assertTrue(output.stdout().contains("npm run dev"));
		assertTrue(output.stdout().contains("npm run build"));
	}

	@Test
	void tasksListsNpmScriptsForNodeViteFixture() {
		CommandResult output = InitioCli.execute("tasks", FixtureRepositories.nodeVite().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("npm run dev"));
		assertTrue(output.stdout().contains("npm run seed-db"));
		assertTrue(output.stdout().contains("Declared"));
	}

	@Test
	void tasksListsMakefileTargets() {
		CommandResult output = InitioCli.execute("tasks", FixtureRepositories.makeTargets().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("make test"));
		assertTrue(output.stdout().contains("Inferred"));
	}

	@Test
	void tasksListsConfiguredCommandsFromInitioYml() {
		CommandResult output = InitioCli.execute("tasks", FixtureRepositories.withInitioConfig().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("./scripts/integration-test.sh"));
		assertTrue(output.stdout().contains("Configured"));
		assertTrue(output.stdout().contains("initio.yml"));
	}

	@Test
	void tasksFailsCleanlyWhenInitioYmlIsInvalid() {
		CommandResult output = InitioCli.execute("tasks", FixtureRepositories.invalidInitioConfig().toString());

		assertEquals(1, output.exitCode());
		assertTrue(output.stderr().contains("Initio could not analyze this project"));
		assertTrue(output.stderr().contains("initio config validate"));
	}
}
