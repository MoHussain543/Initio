package com.mbh.initio.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record CiExpectation(
		Path workflowFile,
		List<String> javaVersions,
		List<String> nodeVersions,
		List<String> runCommands,
		DetectionSource source
) {
	public CiExpectation {
		Objects.requireNonNull(workflowFile, "workflowFile");
		Objects.requireNonNull(source, "source");
		javaVersions = List.copyOf(Objects.requireNonNull(javaVersions, "javaVersions"));
		nodeVersions = List.copyOf(Objects.requireNonNull(nodeVersions, "nodeVersions"));
		runCommands = List.copyOf(Objects.requireNonNull(runCommands, "runCommands"));
	}
}
