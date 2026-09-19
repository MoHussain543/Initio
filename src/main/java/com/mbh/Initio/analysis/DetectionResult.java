package com.mbh.Initio.analysis;

import com.mbh.Initio.model.DetectedTechnology;
import com.mbh.Initio.model.ProjectMetadata;
import com.mbh.Initio.model.RuntimeRequirement;

import java.util.List;
import java.util.Objects;

public record DetectionResult(
		ProjectMetadata metadata,
		List<DetectedTechnology> technologies,
		List<RuntimeRequirement> runtimeRequirements
) {
	public DetectionResult {
		technologies = List.copyOf(Objects.requireNonNull(technologies, "technologies"));
		runtimeRequirements = List.copyOf(Objects.requireNonNull(runtimeRequirements, "runtimeRequirements"));
	}

	public static DetectionResult empty() {
		return new DetectionResult(null, List.of(), List.of());
	}
}
