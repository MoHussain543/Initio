package com.mbh.initio.system;

import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.ServiceStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class DefaultDockerInspector implements DockerInspector {

	private static final Duration TIMEOUT = Duration.ofSeconds(10);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final CommandExecutor commandExecutor;

	public DefaultDockerInspector(CommandExecutor commandExecutor) {
		this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor");
	}

	@Override
	public List<ServiceStatus> inspectServices(ProjectAnalysis project) {
		List<ServiceRequirement> requirements = project.serviceRequirements();
		if (requirements.isEmpty()) {
			return List.of();
		}
		List<ServiceRequirement> composeRequirements = requirements.stream()
				.filter(ServiceRequirement::composeBacked)
				.toList();
		if (composeRequirements.isEmpty()) {
			return requirements.stream()
					.map(requirement -> ServiceStatus.unverified(requirement.serviceName()))
					.toList();
		}
		if (!isDockerAvailable()) {
			return requirements.stream()
					.map(requirement -> ServiceStatus.unverified(requirement.serviceName()))
					.toList();
		}

		Map<Path, List<ServiceRequirement>> byComposeFile = new LinkedHashMap<>();
		for (ServiceRequirement requirement : composeRequirements) {
			byComposeFile.computeIfAbsent(requirement.composeFile(), ignored -> new ArrayList<>()).add(requirement);
		}

		Map<String, ServiceStatus> statuses = new HashMap<>();
		Path projectRoot = project.projectPath();
		for (Map.Entry<Path, List<ServiceRequirement>> entry : byComposeFile.entrySet()) {
			Map<String, ServiceStatus> composeStatuses = inspectComposeFile(projectRoot, entry.getKey(), entry.getValue());
			statuses.putAll(composeStatuses);
		}

		List<ServiceStatus> ordered = new ArrayList<>();
		for (ServiceRequirement requirement : requirements) {
			ordered.add(statuses.getOrDefault(requirement.serviceName(), ServiceStatus.unverified(requirement.serviceName())));
		}
		return List.copyOf(ordered);
	}

	private boolean isDockerAvailable() {
		CommandResult result = commandExecutor.execute(List.of("docker", "version", "--format", "{{.Server.Version}}"), TIMEOUT);
		return result.exitCode() == 0 && result.stdout() != null && !result.stdout().isBlank();
	}

	private Map<String, ServiceStatus> inspectComposeFile(
			Path projectRoot,
			Path composeFile,
			List<ServiceRequirement> requirements
	) {
		Path composePath = projectRoot.resolve(composeFile).normalize();
		List<String> command = List.of(
				"docker",
				"compose",
				"-f",
				composePath.toString(),
				"ps",
				"--format",
				"json"
		);
		CommandResult result = commandExecutor.execute(command, TIMEOUT);
		if (result.exitCode() != 0) {
			Map<String, ServiceStatus> unverified = new HashMap<>();
			for (ServiceRequirement requirement : requirements) {
				unverified.put(requirement.serviceName(), ServiceStatus.unverified(requirement.serviceName()));
			}
			return unverified;
		}

		Map<String, ServiceStatus> statuses = new HashMap<>();
		for (ServiceRequirement requirement : requirements) {
			statuses.put(requirement.serviceName(), ServiceStatus.stopped(requirement.serviceName()));
		}

		for (JsonNode row : parseRows(result.stdout())) {
			String serviceName = text(row.get("Service"));
			if (serviceName == null) {
				continue;
			}
			if (!statuses.containsKey(serviceName)) {
				continue;
			}
			if (isRunning(text(row.get("State")))) {
				statuses.put(serviceName, ServiceStatus.running(serviceName));
			}
		}
		return statuses;
	}

	private static List<JsonNode> parseRows(String stdout) {
		if (stdout == null || stdout.isBlank()) {
			return List.of();
		}
		try {
			JsonNode root = MAPPER.readTree(stdout.trim());
			if (root.isArray()) {
				List<JsonNode> rows = new ArrayList<>();
				root.forEach(rows::add);
				return rows;
			}
			return List.of(root);
		} catch (Exception exception) {
			List<JsonNode> rows = new ArrayList<>();
			for (String line : stdout.lines().filter(line -> !line.isBlank()).toList()) {
				try {
					rows.add(MAPPER.readTree(line));
				} catch (Exception ignored) {
					// skip malformed line
				}
			}
			return rows;
		}
	}

	private static boolean isRunning(String state) {
		if (state == null) {
			return false;
		}
		String normalized = state.toLowerCase(Locale.ROOT);
		return normalized.contains("running") || normalized.contains("up");
	}

	private static String text(JsonNode node) {
		if (node == null || node.isNull()) {
			return null;
		}
		String value = node.asString().trim();
		return value.isEmpty() ? null : value;
	}
}
