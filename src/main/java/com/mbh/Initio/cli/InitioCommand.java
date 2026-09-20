package com.mbh.initio.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
		name = "initio",
		mixinStandardHelpOptions = true,
		version = "0.0.1-SNAPSHOT",
		description = "Local-first developer environment diagnostic and project onboarding tool.",
		subcommands = {CheckCommand.class, InfoCommand.class}
)
public class InitioCommand implements Runnable {

	@Override
	public void run() {
		CommandLine.usage(this, System.out);
	}
}
