package com.mbh.initio.detector.node;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.TechnologyCategory;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeDetectorTest {

	private final NodeDetector detector = new NodeDetector();

	@Test
	void doesNotSupportRepositoriesWithoutPackageJson() {
		assertFalse(detector.supports(new ProjectContext(FixtureRepositories.empty())));
	}

	@Test
	void detectsNodeViteProjectAtRepositoryRoot() {
		DetectionResult result = detector.detect(new ProjectContext(FixtureRepositories.nodeVite()));

		assertEquals("node-vite-demo", result.metadata().name());
		assertTrue(hasTechnology(result, "TypeScript", TechnologyCategory.LANGUAGE));
		assertTrue(hasTechnology(result, "Vite", TechnologyCategory.FRAMEWORK));
		assertTrue(hasTechnology(result, "npm", TechnologyCategory.PACKAGE_MANAGER));
		assertEquals(1, result.runtimeRequirements().size());
		assertEquals(">=22", result.runtimeRequirements().getFirst().requiredVersion());
		assertEquals(Path.of("package.json"), result.runtimeRequirements().getFirst().source().file());
	}

	@Test
	void preservesSeparateNodeRequirementsForRootAndFrontend() {
		DetectionResult result = detector.detect(new ProjectContext(FixtureRepositories.fullstack()));

		assertEquals(2, result.runtimeRequirements().size());
		RuntimeRequirement root = result.runtimeRequirements().get(0);
		RuntimeRequirement frontend = result.runtimeRequirements().get(1);
		assertEquals(">=20", root.requiredVersion());
		assertEquals(Path.of("package.json"), root.source().file());
		assertEquals("^22", frontend.requiredVersion());
		assertEquals(Path.of("frontend", "package.json"), frontend.source().file());
		assertTrue(hasTechnology(result, "React", TechnologyCategory.FRAMEWORK));
		assertTrue(hasTechnology(result, "npm", TechnologyCategory.PACKAGE_MANAGER));
	}

	private static boolean hasTechnology(DetectionResult result, String name, TechnologyCategory category) {
		return result.technologies().stream()
				.anyMatch(technology -> technology.name().equals(name) && technology.category() == category);
	}
}
