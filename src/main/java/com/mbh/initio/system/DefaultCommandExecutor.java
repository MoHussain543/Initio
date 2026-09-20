package com.mbh.initio.system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class DefaultCommandExecutor implements CommandExecutor {

	@Override
	public CommandResult execute(List<String> command, Duration timeout) {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(false);
		try {
			Process process = builder.start();
			boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
			if (!finished) {
				process.destroyForcibly();
				return new CommandResult(-1, "", "Command timed out");
			}
			String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
			return new CommandResult(process.exitValue(), stdout, stderr);
		} catch (IOException | InterruptedException exception) {
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			return new CommandResult(-1, "", exception.getMessage() == null ? "Command failed" : exception.getMessage());
		}
	}
}
