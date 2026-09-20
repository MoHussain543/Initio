package com.mbh.initio.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record ProjectAnalysis(
		Path projectPath,
		ProjectMetadata metadata,
		List<DetectedTechnology> technologies,
		List<RuntimeRequirement> runtimeRequirements,
		List<EnvironmentVariableRequirement> environmentVariableRequirements,
		List<ServiceRequirement> serviceRequirements,
		List<PortExpectation> portExpectations,
		List<ProjectCommand> projectCommands,
		List<CiExpectation> ciExpectations
) {
	public ProjectAnalysis {
		Objects.requireNonNull(projectPath, "projectPath");
		Objects.requireNonNull(metadata, "metadata");
		technologies = List.copyOf(Objects.requireNonNull(technologies, "technologies"));
		runtimeRequirements = List.copyOf(Objects.requireNonNull(runtimeRequirements, "runtimeRequirements"));
		environmentVariableRequirements = List.copyOf(
				Objects.requireNonNull(environmentVariableRequirements, "environmentVariableRequirements")
		);
		serviceRequirements = List.copyOf(Objects.requireNonNull(serviceRequirements, "serviceRequirements"));
		portExpectations = List.copyOf(Objects.requireNonNull(portExpectations, "portExpectations"));
		projectCommands = List.copyOf(Objects.requireNonNull(projectCommands, "projectCommands"));
		ciExpectations = List.copyOf(Objects.requireNonNull(ciExpectations, "ciExpectations"));
	}

	public List<DetectedTechnology> technologies(TechnologyCategory category) {
		Objects.requireNonNull(category, "category");
		return technologies.stream()
				.filter(technology -> technology.category() == category)
				.toList();
	}
}
