package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.ServiceStatus;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MissingRequiredServiceRuleTest {

	@Test
	void reportsStoppedComposeServicesWhenDockerIsAvailable() {
		DetectionSource source = new DetectionSource(Path.of("docker-compose.yml"), "Compose", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				List.of(),
				List.of(),
				List.of(new ServiceRequirement("postgres", Path.of("docker-compose.yml"), "postgres:16", List.of(5432), source)),
				List.of(),
				List.of(),
				List.of()
		);
		AnalysisContext context = new AnalysisContext(
				project,
				new LocalEnvironmentAnalysis(
						List.of(InstalledRuntime.available("docker", "27.0.0")),
						List.of(),
						List.of(ServiceStatus.stopped("postgres")),
						List.of()
				)
		);

		var issues = new MissingRequiredServiceRule().evaluate(context);

		assertEquals(1, issues.size());
		assertTrue(issues.getFirst().title().contains("postgres is not running"));
	}
}
