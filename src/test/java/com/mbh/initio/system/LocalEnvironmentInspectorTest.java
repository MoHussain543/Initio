package com.mbh.initio.system;

import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortObservation;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.ServiceState;
import com.mbh.initio.model.VerificationState;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalEnvironmentInspectorTest {

	@Test
	void verifiesConfiguredServiceAgainstItsConfiguredPortInsteadOfLeavingItUnverified() {
		DetectionSource source = new DetectionSource(Path.of("initio.yml"), "Configured in initio.yml", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				List.of(),
				List.of(),
				List.of(ServiceRequirement.configured("redis", 6379, source)),
				List.of(new PortExpectation(6379, PortRole.EXPECTED_SERVICE, "redis", source)),
				List.of(),
				List.of()
		);

		RuntimeInspector runtimeInspector = new RuntimeInspector() {
			@Override
			public com.mbh.initio.model.InstalledRuntime inspectJava() {
				throw new AssertionError("not needed");
			}

			@Override
			public com.mbh.initio.model.InstalledRuntime inspectNode() {
				throw new AssertionError("not needed");
			}

			@Override
			public com.mbh.initio.model.InstalledRuntime inspectDocker() {
				throw new AssertionError("not needed");
			}
		};
		DockerInspector dockerInspector = analysisProject -> List.of();
		PortInspector portInspector = ports -> List.of(PortObservation.listening(6379, "redis-server"));

		LocalEnvironmentAnalysis analysis = new LocalEnvironmentInspector(runtimeInspector, dockerInspector, portInspector)
				.inspect(project);

		Optional<com.mbh.initio.model.ServiceStatus> redis = analysis.serviceStatus("redis");
		assertEquals(VerificationState.VERIFIED, redis.orElseThrow().verificationState());
		assertEquals(ServiceState.RUNNING, redis.orElseThrow().state());
	}

	@Test
	void doesNotTrustAnUnrelatedProcessListeningOnTheConfiguredPort() {
		DetectionSource source = new DetectionSource(Path.of("initio.yml"), "Configured in initio.yml", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				List.of(),
				List.of(),
				List.of(ServiceRequirement.configured("redis", 6379, source)),
				List.of(new PortExpectation(6379, PortRole.EXPECTED_SERVICE, "redis", source)),
				List.of(),
				List.of()
		);

		RuntimeInspector runtimeInspector = new RuntimeInspector() {
			@Override
			public com.mbh.initio.model.InstalledRuntime inspectJava() {
				throw new AssertionError("not needed");
			}

			@Override
			public com.mbh.initio.model.InstalledRuntime inspectNode() {
				throw new AssertionError("not needed");
			}

			@Override
			public com.mbh.initio.model.InstalledRuntime inspectDocker() {
				throw new AssertionError("not needed");
			}
		};
		DockerInspector dockerInspector = analysisProject -> List.of();
		// Something unrelated (a dev server) happens to be listening on redis's configured port.
		PortInspector portInspector = ports -> List.of(PortObservation.listening(6379, "node"));

		LocalEnvironmentAnalysis analysis = new LocalEnvironmentInspector(runtimeInspector, dockerInspector, portInspector)
				.inspect(project);

		Optional<com.mbh.initio.model.ServiceStatus> redis = analysis.serviceStatus("redis");
		assertEquals(VerificationState.UNVERIFIED, redis.orElseThrow().verificationState());
	}

	@Test
	void marksConfiguredServiceStoppedWhenItsPortIsFree() {
		DetectionSource source = new DetectionSource(Path.of("initio.yml"), "Configured in initio.yml", DetectionConfidence.HIGH);
		ProjectAnalysis project = new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				List.of(),
				List.of(),
				List.of(ServiceRequirement.configured("redis", 6379, source)),
				List.of(new PortExpectation(6379, PortRole.EXPECTED_SERVICE, "redis", source)),
				List.of(),
				List.of()
		);

		RuntimeInspector runtimeInspector = new RuntimeInspector() {
			@Override
			public com.mbh.initio.model.InstalledRuntime inspectJava() {
				throw new AssertionError("not needed");
			}

			@Override
			public com.mbh.initio.model.InstalledRuntime inspectNode() {
				throw new AssertionError("not needed");
			}

			@Override
			public com.mbh.initio.model.InstalledRuntime inspectDocker() {
				throw new AssertionError("not needed");
			}
		};
		DockerInspector dockerInspector = analysisProject -> List.of();
		PortInspector portInspector = ports -> List.of(PortObservation.free(6379));

		LocalEnvironmentAnalysis analysis = new LocalEnvironmentInspector(runtimeInspector, dockerInspector, portInspector)
				.inspect(project);

		Optional<com.mbh.initio.model.ServiceStatus> redis = analysis.serviceStatus("redis");
		assertEquals(VerificationState.VERIFIED, redis.orElseThrow().verificationState());
		assertEquals(ServiceState.STOPPED, redis.orElseThrow().state());
	}
}
