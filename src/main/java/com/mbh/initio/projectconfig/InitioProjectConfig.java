package com.mbh.initio.projectconfig;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record InitioProjectConfig(
		List<String> requiredEnvironmentVariables,
		Map<String, String> runtimes,
		List<ConfiguredCommand> commands,
		List<ConfiguredService> services,
		List<DiagnosticSuppression> suppressions
) {
	public static InitioProjectConfig empty() {
		return new InitioProjectConfig(List.of(), Map.of(), List.of(), List.of(), List.of());
	}

	public InitioProjectConfig {
		requiredEnvironmentVariables = List.copyOf(
				Objects.requireNonNull(requiredEnvironmentVariables, "requiredEnvironmentVariables")
		);
		runtimes = Map.copyOf(Objects.requireNonNull(runtimes, "runtimes"));
		commands = List.copyOf(Objects.requireNonNull(commands, "commands"));
		services = List.copyOf(Objects.requireNonNull(services, "services"));
		suppressions = List.copyOf(Objects.requireNonNull(suppressions, "suppressions"));
	}
}
