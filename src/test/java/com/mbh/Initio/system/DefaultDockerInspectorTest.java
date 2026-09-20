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
}
