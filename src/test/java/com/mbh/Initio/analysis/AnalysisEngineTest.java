package com.mbh.initio.analysis;

import com.mbh.initio.diagnostic.DiagnosticEngine;
import com.mbh.initio.diagnostic.ReadinessCalculator;
import com.mbh.initio.diagnostic.rules.IncompatibleRuntimeVersionRule;
import com.mbh.initio.diagnostic.rules.MissingEnvironmentVariableRule;
import com.mbh.initio.diagnostic.rules.MissingRuntimeRule;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.system.DockerInspector;
import com.mbh.initio.system.LocalEnvironmentInspector;
import com.mbh.initio.system.PortInspector;
import com.mbh.initio.system.RuntimeInspector;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalysisEngineTest {

	@Test
	void runsDetectInspectDiagnoseAndScorePipeline() {
		RuntimeInspector runtimeInspector = new RuntimeInspector() {
			@Override
			public InstalledRuntime inspectJava() {
				return InstalledRuntime.available("java", "25.0.2");
			}

			@Override
			public InstalledRuntime inspectNode() {
				return InstalledRuntime.available("node", "22.14.0");
			}

			@Override
			public InstalledRuntime inspectDocker() {
				return InstalledRuntime.available("docker", "27.0.0");
			}
		};
		DockerInspector dockerInspector = project -> List.of();
		PortInspector portInspector = ports -> List.of();
		AnalysisEngine engine = new AnalysisEngine(
				ProjectAnalyzers.create(),
				new LocalEnvironmentInspector(runtimeInspector, dockerInspector, portInspector),
				new DiagnosticEngine(List.of(
						new MissingRuntimeRule(),
						new IncompatibleRuntimeVersionRule(),
						new MissingEnvironmentVariableRule()
				)),
				new ReadinessCalculator()
		);

		AnalysisResult result = engine.run(FixtureRepositories.fullstack());

		assertEquals("Fullstack App", result.project().metadata().name());
		assertEquals(2, result.local().installedRuntimes().size());
		assertTrue(result.project().runtimeRequirements().stream().anyMatch(requirement -> "node".equals(requirement.runtime())));
		assertEquals(100, result.readiness().percent());
	}
}
