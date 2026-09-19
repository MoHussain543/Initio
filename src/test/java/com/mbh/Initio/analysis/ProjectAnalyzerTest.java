package com.mbh.Initio.analysis;

import com.mbh.Initio.detector.maven.MavenDetector;
import com.mbh.Initio.model.ProjectAnalysis;
import com.mbh.Initio.model.RequirementStatus;
import com.mbh.Initio.model.TechnologyCategory;
import com.mbh.Initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectAnalyzerTest {

	@Test
	void mergesMavenDetectionIntoProjectAnalysis() {
		ProjectAnalysis analysis = new ProjectAnalyzer(new MavenDetector())
				.analyze(FixtureRepositories.springMaven());

		assertEquals("Spring Maven Demo", analysis.metadata().name());
		assertEquals(FixtureRepositories.springMaven().toAbsolutePath().normalize(), analysis.projectPath());
		assertEquals(1, analysis.technologies(TechnologyCategory.LANGUAGE).size());
		assertEquals("Java", analysis.technologies(TechnologyCategory.LANGUAGE).getFirst().name());
		assertEquals("Spring Boot", analysis.technologies(TechnologyCategory.FRAMEWORK).getFirst().name());
		assertEquals("Maven", analysis.technologies(TechnologyCategory.BUILD_TOOL).getFirst().name());
		assertEquals("Maven Wrapper", analysis.technologies(TechnologyCategory.TOOL).getFirst().name());
		assertEquals("25", analysis.runtimeRequirements().getFirst().requiredVersion());
		assertEquals(RequirementStatus.NOT_CHECKED, analysis.runtimeRequirements().getFirst().status());
	}

	@Test
	void usesDirectoryNameWhenNoDetectorContributesMetadata() {
		ProjectAnalysis analysis = new ProjectAnalyzer().analyze(FixtureRepositories.empty());

		assertEquals("empty", analysis.metadata().name());
		assertTrue(analysis.technologies().isEmpty());
		assertTrue(analysis.runtimeRequirements().isEmpty());
	}

	@Test
	void defaultAnalyzerIncludesMavenDetector() {
		ProjectAnalysis analysis = ProjectAnalyzers.create().analyze(FixtureRepositories.mavenPlain());

		assertEquals("Plain App", analysis.metadata().name());
		assertEquals("21", analysis.runtimeRequirements().getFirst().requiredVersion());
	}

	@Test
	void rejectsAMissingDirectory() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new ProjectAnalyzer().analyze(FixtureRepositories.empty().resolve("missing"))
		);
	}
}
