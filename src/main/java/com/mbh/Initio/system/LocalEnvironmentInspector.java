package com.mbh.initio.system;

import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.EnvironmentVariableStatus;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.RuntimeRequirement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class LocalEnvironmentInspector {

	private final RuntimeInspector runtimeInspector;

	public LocalEnvironmentInspector(RuntimeInspector runtimeInspector) {
		this.runtimeInspector = Objects.requireNonNull(runtimeInspector, "runtimeInspector");
	}

	public LocalEnvironmentAnalysis inspect(ProjectAnalysis project) {
		Map<String, InstalledRuntime> runtimes = new LinkedHashMap<>();
		for (RuntimeRequirement requirement : project.runtimeRequirements()) {
			String runtime = requirement.runtime().toLowerCase();
			if (runtimes.containsKey(runtime)) {
				continue;
			}
			runtimes.put(runtime, inspectRuntime(runtime));
		}

		EnvironmentProvider environmentProvider = new DefaultEnvironmentProvider(project.projectPath());
		List<EnvironmentVariableStatus> environmentStatuses = new ArrayList<>();
		for (EnvironmentVariableRequirement requirement : project.environmentVariableRequirements()) {
			environmentStatuses.add(environmentProvider.inspect(requirement.name()));
		}

		return new LocalEnvironmentAnalysis(
				List.copyOf(runtimes.values()),
				List.copyOf(environmentStatuses)
		);
	}

	private InstalledRuntime inspectRuntime(String runtime) {
		return switch (runtime) {
			case "java" -> runtimeInspector.inspectJava();
			case "node" -> runtimeInspector.inspectNode();
			default -> InstalledRuntime.unverified(runtime);
		};
	}
}
