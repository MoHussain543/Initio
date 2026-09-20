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
import static org.junit.jupiter.api.Assertions.assertFalse;
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

	@Test
	void reportsConfiguredServiceWhenPortIsFree() {
		DetectionSource source = new DetectionSource(Path.of("initio.yml"), "Configured", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				List.of(),
				List.of(),
				List.of(ServiceRequirement.configured("redis", 6379, source)),
				List.of(),
				List.of(),
				List.of()
		);
		AnalysisContext context = new AnalysisContext(
				project,
				new LocalEnvironmentAnalysis(
						List.of(),
						List.of(),
						List.of(ServiceStatus.stopped("redis")),
						List.of()
				)
		);

		var issues = new MissingRequiredServiceRule().evaluate(context);

		assertEquals(1, issues.size());
		assertTrue(issues.getFirst().detail().contains("could not be detected on port 6379"));
		assertFalse(issues.getFirst().detail().toLowerCase().contains("container"));
	}

	@Test
	void doesNotReportConfiguredServiceWhenPortIsListening() {
		DetectionSource source = new DetectionSource(Path.of("initio.yml"), "Configured", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				List.of(),
				List.of(),
				List.of(ServiceRequirement.configured("redis", 6379, source)),
				List.of(),
				List.of(),
				List.of()
		);
		AnalysisContext context = new AnalysisContext(
				project,
				new LocalEnvironmentAnalysis(
						List.of(),
						List.of(),
						List.of(ServiceStatus.running("redis")),
						List.of()
				)
		);

		assertEquals(0, new MissingRequiredServiceRule().evaluate(context).size());
	}

	@Test
	void leavesConfiguredServiceUnverifiedWhenPortIsUnknown() {
		DetectionSource source = new DetectionSource(Path.of("initio.yml"), "Configured", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				List.of(),
				List.of(),
				List.of(ServiceRequirement.configured("redis", 6379, source)),
				List.of(),
				List.of(),
				List.of()
		);
		AnalysisContext context = new AnalysisContext(
				project,
				new LocalEnvironmentAnalysis(
						List.of(),
						List.of(),
						List.of(ServiceStatus.unverified("redis")),
						List.of()
				)
		);

		assertEquals(0, new MissingRequiredServiceRule().evaluate(context).size());
	}
}
