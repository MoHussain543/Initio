package com.mbh.initio.detector.node;

import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageJsonParserTest {

	private final PackageJsonParser parser = new PackageJsonParser();

	@Test
	void readsEnginesAndDependenciesFromNodeViteFixture() {
		PackageJson packageJson = parser.parse(FixtureRepositories.nodeVite().resolve("package.json"));

		assertEquals("node-vite-demo", packageJson.name());
		assertEquals(">=22", packageJson.enginesNode());
		assertTrue(packageJson.typescript());
		assertTrue(packageJson.vite());
	}

	@Test
	void readsFrontendPackageJsonFromFullstackFixture() {
		PackageJson packageJson = parser.parse(
				FixtureRepositories.fullstack().resolve("frontend/package.json")
		);

		assertEquals("fullstack-frontend", packageJson.name());
		assertEquals("^22", packageJson.enginesNode());
		assertTrue(packageJson.react());
		assertTrue(packageJson.vite());
	}
}
