package com.mbh.initio.cli;

import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectPathResolverTest {

	@Test
	void defaultsToTheCurrentDirectory() {
		Path resolved = ProjectPathResolver.resolve(null);

		assertTrue(resolved.isAbsolute());
		assertEquals(Path.of(".").toAbsolutePath().normalize(), resolved);
	}

	@Test
	void normalizesAnExistingDirectory() {
		Path fixture = FixtureRepositories.springMaven();

		assertEquals(fixture.toAbsolutePath().normalize(), ProjectPathResolver.resolve(fixture));
	}

	@Test
	void rejectsAMissingPath() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> ProjectPathResolver.resolve(Path.of("/this/path/does-not-exist-initio"))
		);

		assertTrue(exception.getMessage().contains("does not exist"));
	}

	@Test
	void rejectsAFile(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("readme.txt");
		Files.writeString(file, "not a project");

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> ProjectPathResolver.resolve(file)
		);

		assertTrue(exception.getMessage().contains("not a directory"));
	}
}
