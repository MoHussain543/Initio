package com.mbh.initio.analysis;

import com.mbh.initio.diagnostic.DiagnosticEngine;
import com.mbh.initio.diagnostic.ReadinessCalculator;
import com.mbh.initio.diagnostic.rules.IncompatibleRuntimeVersionRule;
import com.mbh.initio.diagnostic.rules.DockerUnavailableRule;
import com.mbh.initio.diagnostic.rules.MissingEnvironmentVariableRule;
import com.mbh.initio.diagnostic.rules.MissingRequiredServiceRule;
import com.mbh.initio.diagnostic.rules.MissingRuntimeRule;
import com.mbh.initio.system.DefaultCommandExecutor;
import com.mbh.initio.system.DefaultDockerInspector;
import com.mbh.initio.system.DefaultRuntimeInspector;
import com.mbh.initio.system.LocalEnvironmentInspector;

import java.util.List;

public final class AnalysisEngines {

	private AnalysisEngines() {
	}

	public static AnalysisEngine createDefault() {
		var commandExecutor = new DefaultCommandExecutor();
		var runtimeInspector = new DefaultRuntimeInspector(commandExecutor);
		var dockerInspector = new DefaultDockerInspector(commandExecutor);
		var localEnvironmentInspector = new LocalEnvironmentInspector(runtimeInspector, dockerInspector);
		var diagnosticEngine = new DiagnosticEngine(List.of(
				new MissingRuntimeRule(),
				new IncompatibleRuntimeVersionRule(),
				new MissingEnvironmentVariableRule(),
				new DockerUnavailableRule(),
				new MissingRequiredServiceRule()
		));
		return new AnalysisEngine(
				ProjectAnalyzers.create(),
				localEnvironmentInspector,
				diagnosticEngine,
				new ReadinessCalculator()
		);
	}
}
