package com.mbh.initio.analysis;

import com.mbh.initio.detector.ProjectDetector;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.ServiceRequirement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ProjectAnalyzer {

	private final List<ProjectDetector> detectors;

	public ProjectAnalyzer(List<ProjectDetector> detectors) {
		this.detectors = List.copyOf(Objects.requireNonNull(detectors, "detectors"));
	}

	public ProjectAnalyzer(ProjectDetector... detectors) {
		this(List.of(detectors));
	}

	public ProjectAnalysis analyze(Path projectPath) {
		Objects.requireNonNull(projectPath, "projectPath");
		Path root = projectPath.toAbsolutePath().normalize();
		if (!Files.isDirectory(root)) {
			throw new IllegalArgumentException("Project path is not a directory: " + root);
		}

		ProjectContext context = new ProjectContext(root);
		ProjectMetadata metadata = null;
		List<DetectedTechnology> technologies = new ArrayList<>();
		List<RuntimeRequirement> runtimeRequirements = new ArrayList<>();
		List<EnvironmentVariableRequirement> environmentVariableRequirements = new ArrayList<>();
		List<ServiceRequirement> serviceRequirements = new ArrayList<>();
		List<PortExpectation> portExpectations = new ArrayList<>();

		for (ProjectDetector detector : detectors) {
			if (!detector.supports(context)) {
				continue;
			}
			DetectionResult result = detector.detect(context);
			metadata = mergeMetadata(metadata, result.metadata());
			technologies.addAll(result.technologies());
			runtimeRequirements.addAll(result.runtimeRequirements());
			environmentVariableRequirements.addAll(result.environmentVariableRequirements());
			serviceRequirements.addAll(result.serviceRequirements());
			portExpectations.addAll(result.portExpectations());
		}

		return new ProjectAnalysis(
				root,
				fallbackMetadata(metadata, root),
				technologies,
				runtimeRequirements,
				environmentVariableRequirements,
				serviceRequirements,
				portExpectations
		);
	}

	private static ProjectMetadata mergeMetadata(ProjectMetadata existing, ProjectMetadata incoming) {
		if (incoming == null) {
			return existing;
		}
		if (existing == null) {
			return incoming;
		}
		String name = hasText(existing.name()) ? existing.name() : incoming.name();
		String description = hasText(existing.description()) ? existing.description() : incoming.description();
		return new ProjectMetadata(name, description);
	}

	private static ProjectMetadata fallbackMetadata(ProjectMetadata metadata, Path root) {
		if (metadata != null && hasText(metadata.name())) {
			return metadata;
		}
		String directoryName = root.getFileName() != null ? root.getFileName().toString() : root.toString();
		String description = metadata != null ? metadata.description() : null;
		return new ProjectMetadata(directoryName, description);
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
