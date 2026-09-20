package com.mbh.initio.system;

import com.mbh.initio.model.InstalledRuntime;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DefaultRuntimeInspector implements RuntimeInspector {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private static final Pattern NODE_VERSION = Pattern.compile("v([0-9.]+)");
	private static final Pattern DOCKER_VERSION = Pattern.compile("([0-9]+\\.[0-9]+\\.[0-9]+)");
	private static final Pattern JAVA_QUOTED_VERSION = Pattern.compile("(?i)version\\s+\"([^\"]+)\"");
	private static final Pattern JAVA_UNQUOTED_VERSION = Pattern.compile("(?i)version\\s+([0-9][0-9._]+)");
	private static final Pattern JAVA_DISTRIBUTION_VERSION = Pattern.compile(
			"(?i)(?:openjdk|java|jdk|jre)\\s+([0-9][0-9._]+)"
	);

	private final CommandExecutor commandExecutor;

	public DefaultRuntimeInspector(CommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}

	@Override
	public InstalledRuntime inspectJava() {
		CommandResult result = commandExecutor.execute(List.of("java", "--version"), TIMEOUT);
		if (result.exitCode() != 0) {
			return failedInspection("java", result);
		}
		return parseJavaVersion(combinedOutput(result))
				.map(version -> InstalledRuntime.available("java", version))
				.orElseGet(() -> InstalledRuntime.unverified("java"));
	}

	@Override
	public InstalledRuntime inspectNode() {
		return inspect("node", List.of("node", "--version"), NODE_VERSION);
	}

	@Override
	public InstalledRuntime inspectDocker() {
		CommandResult result = commandExecutor.execute(
				List.of("docker", "version", "--format", "{{.Server.Version}}"),
				TIMEOUT
		);
		if (result.exitCode() != 0) {
			return failedInspection("docker", result);
		}
		String version = result.stdout() == null ? "" : result.stdout().trim();
		if (version.isBlank()) {
			return InstalledRuntime.missing("docker");
		}
		Matcher matcher = DOCKER_VERSION.matcher(version);
		if (matcher.find()) {
			version = matcher.group(1);
		}
		return InstalledRuntime.available("docker", version);
	}

	static Optional<String> parseJavaVersion(String output) {
		if (output == null || output.isBlank()) {
			return Optional.empty();
		}
		Matcher quoted = JAVA_QUOTED_VERSION.matcher(output);
		if (quoted.find()) {
			return Optional.of(quoted.group(1).trim());
		}
		Matcher unquoted = JAVA_UNQUOTED_VERSION.matcher(output);
		if (unquoted.find()) {
			return Optional.of(unquoted.group(1).trim());
		}
		Matcher distribution = JAVA_DISTRIBUTION_VERSION.matcher(output);
		if (distribution.find()) {
			return Optional.of(distribution.group(1).trim());
		}
		return Optional.empty();
	}

	private InstalledRuntime inspect(String runtime, List<String> command, Pattern pattern) {
		CommandResult result = commandExecutor.execute(command, TIMEOUT);
		if (result.exitCode() != 0) {
			return failedInspection(runtime, result);
		}
		Matcher matcher = pattern.matcher(combinedOutput(result));
		if (!matcher.find()) {
			return InstalledRuntime.unverified(runtime);
		}
		String version = matcher.group(1);
		if (version == null || version.isBlank()) {
			return InstalledRuntime.unverified(runtime);
		}
		return InstalledRuntime.available(runtime, version);
	}

	private static InstalledRuntime failedInspection(String runtime, CommandResult result) {
		if (result.exitCode() == -1 && result.stderr() != null && result.stderr().contains("timed out")) {
			return InstalledRuntime.unverified(runtime);
		}
		return InstalledRuntime.missing(runtime);
	}

	private static String combinedOutput(CommandResult result) {
		String stdout = result.stdout() == null ? "" : result.stdout();
		String stderr = result.stderr() == null ? "" : result.stderr();
		return stdout + System.lineSeparator() + stderr;
	}
}
