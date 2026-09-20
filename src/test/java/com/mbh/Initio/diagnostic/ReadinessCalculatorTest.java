package com.mbh.initio.diagnostic;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.ReadinessScore;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.TechnologyCategory;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadinessCalculatorTest {

	@Test
	void excludesUnverifiedRequirementsFromReadinessDenominator() {
		DetectionSource source = new DetectionSource(Path.of("pom.xml"), "Declared in pom.xml", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(new DetectedTechnology("Java", TechnologyCategory.LANGUAGE, source)),
				List.of(RuntimeRequirement.declared("java", "25", source)),
				List.of(),
				List.of(),
				List.of()
		);
		LocalEnvironmentAnalysis local = new LocalEnvironmentAnalysis(
				List.of(InstalledRuntime.unverified("java")),
				List.of(),
				List.of()
		);
		AnalysisContext context = new AnalysisContext(project, local);

		ReadinessScore score = new ReadinessCalculator().calculate(context, List.of());

		assertEquals(100, score.percent());
		assertEquals(0, score.verifiedTotal());
		assertEquals(1, score.unverifiedCount());
		assertTrue(score.summary().contains("could not verify"));
	}
}
