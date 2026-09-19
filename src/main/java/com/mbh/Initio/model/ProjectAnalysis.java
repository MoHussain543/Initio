package com.mbh.Initio.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record ProjectAnalysis(
		Path projectPath,
		ProjectMetadata metadata,
		List<DetectedTechnology> technologies,
		List<RuntimeRequirement> runtimeRequirements
) {
	public ProjectAnalysis {
		Objects.requireNonNull(projectPath, "projectPath");
		Objects.requireNonNull(metadata, "metadata");
		technologies = List.copyOf(Objects.requireNonNull(technologies, "technologies"));
		runtimeRequirements = List.copyOf(Objects.requireNonNull(runtimeRequirements, "runtimeRequirements"));
	}

	public List<DetectedTechnology> technologies(TechnologyCategory category) {
		Objects.requireNonNull(category, "category");
		return technologies.stream()
				.filter(technology -> technology.category() == category)
				.toList();
	}
}
