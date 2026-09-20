package com.mbh.initio.cli;

import com.mbh.initio.testsupport.FixtureRepositories;
import com.mbh.initio.testsupport.InitioCli;
import com.mbh.initio.testsupport.InitioCli.CommandResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfoCommandTest {

	@Test
	void infoGroupsTechnologiesByCategory() {
		CommandResult output = InitioCli.execute("info", FixtureRepositories.springMaven().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("Project"));
		assertTrue(output.stdout().contains("Spring Maven Demo"));
		assertTrue(output.stdout().contains("Languages"));
		assertTrue(output.stdout().contains("Java"));
		assertTrue(output.stdout().contains("Frameworks"));
		assertTrue(output.stdout().contains("Spring Boot"));
		assertTrue(output.stdout().contains("Build tools"));
		assertTrue(output.stdout().contains("Maven"));
		assertTrue(output.stdout().contains("Tools"));
		assertTrue(output.stdout().contains("Maven Wrapper"));
		assertTrue(output.stdout().contains("GitHub Actions"));
		assertFalse(output.stdout().contains("Package managers"));
		assertTrue(output.stdout().contains("CI"));
		assertTrue(output.stdout().contains("Java 25"));
		assertTrue(output.stdout().contains("./mvnw test"));
		assertTrue(output.stdout().contains("Commands"));
		assertTrue(output.stdout().contains("initio commands"));
	}

	@Test
	void infoIncludesNodeStackForNodeViteFixture() {
		CommandResult output = InitioCli.execute("info", FixtureRepositories.nodeVite().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("node-vite-demo"));
		assertTrue(output.stdout().contains("TypeScript"));
		assertTrue(output.stdout().contains("Vite"));
		assertTrue(output.stdout().contains("Package managers"));
		assertTrue(output.stdout().contains("npm"));
		assertTrue(output.stdout().contains("Commands"));
		assertTrue(output.stdout().contains("npm run dev"));
	}

	@Test
	void infoListsComposeServicesForDockerizedFixture() {
		CommandResult output = InitioCli.execute("info", FixtureRepositories.dockerized().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("Compose services"));
		assertTrue(output.stdout().contains("postgres"));
		assertTrue(output.stdout().contains("redis"));
		assertTrue(output.stdout().contains("Docker Compose"));
	}

	@Test
	void infoOmitsEmptySectionsForAPlainMavenProject() {
		CommandResult output = InitioCli.execute("info", FixtureRepositories.mavenPlain().toString());

		assertEquals(0, output.exitCode());
		assertTrue(output.stdout().contains("Plain App"));
		assertTrue(output.stdout().contains("Languages"));
		assertTrue(output.stdout().contains("Build tools"));
		assertFalse(output.stdout().contains("Frameworks"));
		assertFalse(output.stdout().contains("Tools"));
	}

	@Test
	void infoFailsCleanlyWhenInitioYmlIsInvalid() {
		CommandResult output = InitioCli.execute("info", FixtureRepositories.invalidInitioConfig().toString());

		assertEquals(1, output.exitCode());
		assertTrue(output.stderr().contains("Initio could not analyze this project"));
		assertTrue(output.stderr().contains("initio config validate"));
	}
}
