package com.mbh.initio.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record LocalEnvironmentAnalysis(
		List<InstalledRuntime> installedRuntimes,
		List<EnvironmentVariableStatus> environmentVariableStatuses
) {
	public LocalEnvironmentAnalysis {
		installedRuntimes = List.copyOf(Objects.requireNonNull(installedRuntimes, "installedRuntimes"));
		environmentVariableStatuses = List.copyOf(
				Objects.requireNonNull(environmentVariableStatuses, "environmentVariableStatuses")
		);
	}

	public Optional<InstalledRuntime> installedRuntime(String runtime) {
		return installedRuntimes.stream()
				.filter(entry -> entry.runtime().equalsIgnoreCase(runtime))
				.findFirst();
	}

	public Optional<EnvironmentVariableStatus> environmentVariableStatus(String name) {
		return environmentVariableStatuses.stream()
				.filter(entry -> entry.name().equals(name))
				.findFirst();
	}
}
