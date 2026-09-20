package com.mbh.initio.detector.node;

import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeProjectLocatorTest {

	@Test
	void findsRootAndNestedPackageJsonFiles() {
		var locations = NodeProjectLocator.locate(new ProjectContext(FixtureRepositories.fullstack()));

		assertEquals(2, locations.size());
		assertEquals(Path.of("package.json"), locations.get(0));
		assertEquals(Path.of("frontend", "package.json"), locations.get(1));
	}

	@Test
	void findsOnlyRootForNodeViteFixture() {
		var locations = NodeProjectLocator.locate(new ProjectContext(FixtureRepositories.nodeVite()));

		assertEquals(1, locations.size());
		assertEquals(Path.of("package.json"), locations.getFirst());
	}

	@Test
	void returnsEmptyWhenNoPackageJsonExists() {
		assertTrue(NodeProjectLocator.locate(new ProjectContext(FixtureRepositories.empty())).isEmpty());
	}
}
