package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.TechnologyCategory;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MissingRuntimeRuleTest {

	@Test
	void reportsMissingJavaRuntimeWhenVerifiedMissing() {
		DetectionSource source = new DetectionSource(Path.of("pom.xml"), "Declared in pom.xml", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(new DetectedTechnology("Java", TechnologyCategory.LANGUAGE, source)),
				List.of(RuntimeRequirement.declared("java", "25", source))
		);
		AnalysisContext context = new AnalysisContext(
				project,
				new LocalEnvironmentAnalysis(List.of(InstalledRuntime.missing("java")))
		);

		List<DiagnosticIssue> issues = new MissingRuntimeRule().evaluate(context);

		assertEquals(1, issues.size());
		assertTrue(issues.getFirst().title().contains("Java is not installed"));
	}
}
