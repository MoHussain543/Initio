package com.mbh.Initio.detector.maven;

import com.mbh.Initio.detector.DetectionException;
import com.mbh.Initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenPomParserTest {

	private final MavenPomParser parser = new MavenPomParser();

	@Test
	void readsNamespacedSpringBootPom() {
		MavenPom pom = parser.parse(FixtureRepositories.springMaven().resolve("pom.xml"));

		assertEquals("demo", pom.artifactId());
		assertEquals("Spring Maven Demo", pom.name());
		assertEquals("org.springframework.boot", pom.parentGroupId());
		assertEquals("spring-boot-starter-parent", pom.parentArtifactId());
		assertEquals("25", pom.javaVersionProperty());
		assertTrue(pom.springBootDependency());
	}

	@Test
	void readsCompilerReleaseWithoutNamespace() {
		MavenPom pom = parser.parse(FixtureRepositories.mavenPlain().resolve("pom.xml"));

		assertEquals("plain-app", pom.artifactId());
		assertEquals("21", pom.compilerReleaseProperty());
		assertNull(pom.javaVersionProperty());
		assertFalse(pom.springBootDependency());
		assertNull(pom.parentArtifactId());
	}

	@Test
	void leavesVersionFieldsEmptyWhenUndeclared() {
		MavenPom pom = parser.parse(FixtureRepositories.mavenNoJavaVersion().resolve("pom.xml"));

		assertEquals("no-java-version", pom.artifactId());
		assertNull(pom.javaVersionProperty());
		assertNull(pom.compilerReleaseProperty());
		assertNull(pom.compilerSourceProperty());
		assertNull(pom.compilerPluginRelease());
		assertNull(pom.compilerPluginSource());
	}

	@Test
	void readsCompilerPluginRelease() {
		MavenPom pom = parser.parse(FixtureRepositories.mavenCompilerPlugin().resolve("pom.xml"));

		assertEquals("17", pom.compilerPluginRelease());
		assertNull(pom.javaVersionProperty());
	}

	@Test
	void rejectsMalformedPom() {
		assertThrows(
				DetectionException.class,
				() -> parser.parse(FixtureRepositories.mavenMalformed().resolve("pom.xml"))
		);
	}
}
