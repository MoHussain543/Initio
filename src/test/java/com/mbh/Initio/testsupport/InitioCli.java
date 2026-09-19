package com.mbh.Initio.testsupport;

import com.mbh.Initio.cli.InitioCommand;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class InitioCli {

	private InitioCli() {
	}

	public static CommandResult execute(String... args) {
		StringWriter stdout = new StringWriter();
		StringWriter stderr = new StringWriter();
		CommandLine commandLine = new CommandLine(new InitioCommand());
		commandLine.setOut(new PrintWriter(stdout, true));
		commandLine.setErr(new PrintWriter(stderr, true));
		int exitCode = commandLine.execute(args);
		return new CommandResult(exitCode, stdout.toString(), stderr.toString());
	}

	public record CommandResult(int exitCode, String stdout, String stderr) {
	}
}
