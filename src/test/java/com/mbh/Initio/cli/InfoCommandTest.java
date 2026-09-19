package com.mbh.Initio.cli;

import com.mbh.Initio.testsupport.FixtureRepositories;
import com.mbh.Initio.testsupport.InitioCli;
import com.mbh.Initio.testsupport.InitioCli.CommandResult;
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
		assertFalse(output.stdout().contains("Package managers"));
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
}
