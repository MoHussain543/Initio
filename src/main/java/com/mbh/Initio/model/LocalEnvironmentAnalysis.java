package com.mbh.initio.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record LocalEnvironmentAnalysis(
		List<InstalledRuntime> installedRuntimes,
		List<EnvironmentVariableStatus> environmentVariableStatuses,
		List<ServiceStatus> serviceStatuses
) {
	public LocalEnvironmentAnalysis {
		installedRuntimes = List.copyOf(Objects.requireNonNull(installedRuntimes, "installedRuntimes"));
		environmentVariableStatuses = List.copyOf(
				Objects.requireNonNull(environmentVariableStatuses, "environmentVariableStatuses")
		);
		serviceStatuses = List.copyOf(Objects.requireNonNull(serviceStatuses, "serviceStatuses"));
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

	public Optional<ServiceStatus> serviceStatus(String serviceName) {
		return serviceStatuses.stream()
				.filter(entry -> entry.serviceName().equals(serviceName))
				.findFirst();
	}
}
