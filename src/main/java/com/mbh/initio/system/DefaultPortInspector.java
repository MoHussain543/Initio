package com.mbh.initio.system;

import com.mbh.initio.model.PortObservation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class DefaultPortInspector implements PortInspector {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private final CommandExecutor commandExecutor;

	public DefaultPortInspector(CommandExecutor commandExecutor) {
		this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor");
	}

	@Override
	public List<PortObservation> inspectPorts(List<Integer> ports) {
		Set<Integer> unique = new LinkedHashSet<>(ports);
		List<PortObservation> observations = new ArrayList<>();
		for (int port : unique) {
			observations.add(inspectPort(port));
		}
		return List.copyOf(observations);
	}

	private PortObservation inspectPort(int port) {
		List<String> command = List.of("lsof", "-iTCP:" + port, "-sTCP:LISTEN", "-n", "-P");
		CommandResult result = commandExecutor.execute(command, TIMEOUT);
		if (result.exitCode() == -1) {
			return PortObservation.unverified(port);
		}
		if (result.exitCode() != 0) {
			String output = combinedOutput(result);
			if (output.isBlank() || output.toLowerCase().contains("not found")) {
				return PortObservation.free(port);
			}
			return PortObservation.unverified(port);
		}
		String occupant = parseOccupant(result.stdout());
		if (occupant == null) {
			return PortObservation.free(port);
		}
		return PortObservation.listening(port, occupant);
	}

	private static String parseOccupant(String stdout) {
		if (stdout == null || stdout.isBlank()) {
			return null;
		}
		for (String line : stdout.lines().toList()) {
			if (line.startsWith("COMMAND")) {
				continue;
			}
			String trimmed = line.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			String[] parts = trimmed.split("\\s+");
			if (parts.length > 0 && !parts[0].isBlank()) {
				return parts[0];
			}
		}
		return null;
	}

	private static String combinedOutput(CommandResult result) {
		String stdout = result.stdout() == null ? "" : result.stdout();
		String stderr = result.stderr() == null ? "" : result.stderr();
		return (stdout + System.lineSeparator() + stderr).trim();
	}
}
