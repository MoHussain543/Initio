package com.mbh.initio.system;

import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.ServiceState;
import com.mbh.initio.model.ServiceStatus;
import com.mbh.initio.model.TechnologyCategory;
import com.mbh.initio.model.VerificationState;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultDockerInspectorTest {

	@Test
	void marksServicesStoppedWhenComposePsShowsNoRunningContainers() {
		DetectionSource source = new DetectionSource(Path.of("docker-compose.yml"), "Compose", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				FixtureRepositories.dockerized(),
				new ProjectMetadata("dockerized", null),
				List.of(new DetectedTechnology("Docker Compose", TechnologyCategory.TOOL, source)),
				List.of(),
				List.of(),
				List.of(
						new ServiceRequirement("postgres", Path.of("docker-compose.yml"), "postgres:16", List.of(5432), source),
						new ServiceRequirement("redis", Path.of("docker-compose.yml"), "redis:7", List.of(6379), source)
				),
				List.of(),
				List.of(),
				List.of()
		);
		CommandExecutor commandExecutor = new CommandExecutor() {
			@Override
			public CommandResult execute(List<String> command, Duration timeout) {
				if (command.contains("version")) {
					return new CommandResult(0, "27.0.0", "");
				}
				if (command.contains("ps")) {
					return new CommandResult(0, "[]", "");
				}
				return new CommandResult(1, "", "unexpected");
			}
		};

		List<ServiceStatus> statuses = new DefaultDockerInspector(commandExecutor).inspectServices(project);

		assertEquals(2, statuses.size());
		assertEquals(ServiceState.STOPPED, statuses.get(0).state());
		assertEquals(VerificationState.VERIFIED, statuses.get(0).verificationState());
		assertEquals("postgres", statuses.get(0).serviceName());
	}

	@Test
	void doesNotInspectComposeForConfiguredServiceHints() {
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
		CommandExecutor commandExecutor = (command, timeout) -> {
			throw new AssertionError("docker should not be invoked for configured service hints: " + command);
		};

		List<ServiceStatus> statuses = new DefaultDockerInspector(commandExecutor).inspectServices(project);

		// Configured (non-compose) service hints are left for LocalEnvironmentInspector to
		// verify against the port scan, so the docker inspector must not pre-empt them here.
		assertEquals(List.of(), statuses);
	}
}
