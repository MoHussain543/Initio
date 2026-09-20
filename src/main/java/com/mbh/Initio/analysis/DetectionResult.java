package com.mbh.initio.analysis;

import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.RuntimeRequirement;

import java.util.List;
import java.util.Objects;

public record DetectionResult(
		ProjectMetadata metadata,
		List<DetectedTechnology> technologies,
		List<RuntimeRequirement> runtimeRequirements,
		List<EnvironmentVariableRequirement> environmentVariableRequirements
) {
	public DetectionResult {
		technologies = List.copyOf(Objects.requireNonNull(technologies, "technologies"));
		runtimeRequirements = List.copyOf(Objects.requireNonNull(runtimeRequirements, "runtimeRequirements"));
		environmentVariableRequirements = List.copyOf(
				Objects.requireNonNull(environmentVariableRequirements, "environmentVariableRequirements")
		);
	}

	public static DetectionResult empty() {
		return new DetectionResult(null, List.of(), List.of(), List.of());
	}
}
