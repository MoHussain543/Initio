package com.mbh.initio.analysis;

import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.ProjectCommand;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.ServiceRequirement;

import java.util.List;
import java.util.Objects;

public record DetectionResult(
		ProjectMetadata metadata,
		List<DetectedTechnology> technologies,
		List<RuntimeRequirement> runtimeRequirements,
		List<EnvironmentVariableRequirement> environmentVariableRequirements,
		List<ServiceRequirement> serviceRequirements,
		List<PortExpectation> portExpectations,
		List<ProjectCommand> projectCommands
) {
	public DetectionResult {
		technologies = List.copyOf(Objects.requireNonNull(technologies, "technologies"));
		runtimeRequirements = List.copyOf(Objects.requireNonNull(runtimeRequirements, "runtimeRequirements"));
		environmentVariableRequirements = List.copyOf(
				Objects.requireNonNull(environmentVariableRequirements, "environmentVariableRequirements")
		);
		serviceRequirements = List.copyOf(Objects.requireNonNull(serviceRequirements, "serviceRequirements"));
		portExpectations = List.copyOf(Objects.requireNonNull(portExpectations, "portExpectations"));
		projectCommands = List.copyOf(Objects.requireNonNull(projectCommands, "projectCommands"));
	}

	public static DetectionResult empty() {
		return new DetectionResult(null, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
	}
}
