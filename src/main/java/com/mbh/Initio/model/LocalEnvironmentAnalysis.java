package com.mbh.initio.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record LocalEnvironmentAnalysis(List<InstalledRuntime> installedRuntimes) {
	public LocalEnvironmentAnalysis {
		installedRuntimes = List.copyOf(Objects.requireNonNull(installedRuntimes, "installedRuntimes"));
	}

	public Optional<InstalledRuntime> installedRuntime(String runtime) {
		return installedRuntimes.stream()
				.filter(entry -> entry.runtime().equalsIgnoreCase(runtime))
				.findFirst();
	}
}
