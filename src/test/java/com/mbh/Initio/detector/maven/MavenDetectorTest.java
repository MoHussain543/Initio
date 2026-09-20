package com.mbh.initio.detector.maven;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.RequirementStatus;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.TechnologyCategory;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenDetectorTest {

	private final MavenDetector detector = new MavenDetector();

	@Test
	void supportsOnlyWhenPomExists() {
		assertTrue(detector.supports(new ProjectContext(FixtureRepositories.springMaven())));
		assertFalse(detector.supports(new ProjectContext(FixtureRepositories.empty())));
	}

	@Test
	void detectsJavaLanguageMavenBuildToolSpringBootAndWrapper() {
		DetectionResult result = detector.detect(new ProjectContext(FixtureRepositories.springMaven()));

		assertEquals("Spring Maven Demo", result.metadata().name());
		assertEquals(TechnologyCategory.LANGUAGE, technology(result, "Java").category());
		assertEquals(TechnologyCategory.BUILD_TOOL, technology(result, "Maven").category());
		assertEquals(TechnologyCategory.FRAMEWORK, technology(result, "Spring Boot").category());
		assertEquals(TechnologyCategory.TOOL, technology(result, "Maven Wrapper").category());
		assertEquals(Path.of("mvnw"), technology(result, "Maven Wrapper").source().file());

		assertEquals(1, result.runtimeRequirements().size());
		RuntimeRequirement java = result.runtimeRequirements().getFirst();
		assertEquals("java", java.runtime());
		assertEquals("25", java.requiredVersion());
		assertEquals(RequirementStatus.NOT_CHECKED, java.status());
	}

	@Test
	void readsJavaVersionFromCompilerReleaseWithoutSpringBootOrWrapper() {
		DetectionResult result = detector.detect(new ProjectContext(FixtureRepositories.mavenPlain()));

		assertEquals(TechnologyCategory.LANGUAGE, technology(result, "Java").category());
		assertEquals(TechnologyCategory.BUILD_TOOL, technology(result, "Maven").category());
		assertFalse(hasTechnology(result, "Spring Boot"));
		assertFalse(hasTechnology(result, "Maven Wrapper"));
		assertEquals("21", result.runtimeRequirements().getFirst().requiredVersion());
		assertEquals(RequirementStatus.NOT_CHECKED, result.runtimeRequirements().getFirst().status());
	}

	@Test
	void reportsJavaLanguageWithoutInventingAVersion() {
		DetectionResult result = detector.detect(new ProjectContext(FixtureRepositories.mavenNoJavaVersion()));

		assertEquals("no-java-version", result.metadata().name());
		assertTrue(hasTechnology(result, "Java"));
		assertTrue(result.runtimeRequirements().isEmpty());
	}

	@Test
	void readsJavaVersionFromCompilerPlugin() {
		DetectionResult result = detector.detect(new ProjectContext(FixtureRepositories.mavenCompilerPlugin()));

		assertEquals("17", result.runtimeRequirements().getFirst().requiredVersion());
	}

	private static DetectedTechnology technology(DetectionResult result, String name) {
		return result.technologies().stream()
				.filter(technology -> technology.name().equals(name))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Missing technology: " + name));
	}

	private static boolean hasTechnology(DetectionResult result, String name) {
		return result.technologies().stream().anyMatch(technology -> technology.name().equals(name));
	}
}
