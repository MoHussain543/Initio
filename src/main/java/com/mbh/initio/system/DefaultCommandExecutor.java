package com.mbh.initio.system;

import java.io.IOException;
import java.io.InputStream;
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
			// Streams must be drained concurrently with waitFor: if the child fills the OS
			// pipe buffer for stdout/stderr before we read it, it blocks on write and waitFor
			// never returns, deadlocking this call.
			StreamReader stdoutReader = new StreamReader(process.getInputStream());
			StreamReader stderrReader = new StreamReader(process.getErrorStream());
			Thread stdoutThread = new Thread(stdoutReader, "initio-command-stdout");
			Thread stderrThread = new Thread(stderrReader, "initio-command-stderr");
			stdoutThread.setDaemon(true);
			stderrThread.setDaemon(true);
			stdoutThread.start();
			stderrThread.start();

			boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
			if (!finished) {
				process.destroyForcibly();
			}
			stdoutThread.join(timeout.toMillis());
			stderrThread.join(timeout.toMillis());
			if (!finished) {
				return new CommandResult(-1, stdoutReader.content(), "Command timed out");
			}
			return new CommandResult(process.exitValue(), stdoutReader.content(), stderrReader.content());
		} catch (IOException | InterruptedException exception) {
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			return new CommandResult(-1, "", exception.getMessage() == null ? "Command failed" : exception.getMessage());
		}
	}

	private static final class StreamReader implements Runnable {
		private final InputStream input;
		private volatile String content = "";

		private StreamReader(InputStream input) {
			this.input = input;
		}

		@Override
		public void run() {
			try {
				content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
			} catch (IOException ignored) {
				content = "";
			}
		}

		private String content() {
			return content;
		}
	}
}
