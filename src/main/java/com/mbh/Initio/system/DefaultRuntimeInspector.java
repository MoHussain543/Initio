package com.mbh.initio.system;

import com.mbh.initio.model.InstalledRuntime;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DefaultRuntimeInspector implements RuntimeInspector {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private static final Pattern JAVA_VERSION = Pattern.compile("version \"([^\"]+)\"|version ([0-9.]+)");
	private static final Pattern NODE_VERSION = Pattern.compile("v([0-9.]+)");

	private final CommandExecutor commandExecutor;

	public DefaultRuntimeInspector(CommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}

	@Override
	public InstalledRuntime inspectJava() {
		return inspect("java", List.of("java", "--version"), JAVA_VERSION);
	}

	@Override
	public InstalledRuntime inspectNode() {
		return inspect("node", List.of("node", "--version"), NODE_VERSION);
	}

	private InstalledRuntime inspect(String runtime, List<String> command, Pattern pattern) {
		CommandResult result = commandExecutor.execute(command, TIMEOUT);
		if (result.exitCode() != 0) {
			if (result.exitCode() == -1 && result.stderr() != null && result.stderr().contains("timed out")) {
				return InstalledRuntime.unverified(runtime);
			}
			return InstalledRuntime.missing(runtime);
		}
		String output = result.stdout() + System.lineSeparator() + result.stderr();
		Matcher matcher = pattern.matcher(output);
		if (!matcher.find()) {
			return InstalledRuntime.unverified(runtime);
		}
		String version = matcher.group(1);
		if (version == null && matcher.groupCount() >= 2) {
			version = matcher.group(2);
		}
		if (version == null || version.isBlank()) {
			return InstalledRuntime.unverified(runtime);
		}
		return InstalledRuntime.available(runtime, version);
	}
}
