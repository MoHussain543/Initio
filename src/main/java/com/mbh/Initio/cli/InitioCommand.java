package com.mbh.Initio.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
		name = "initio",
		mixinStandardHelpOptions = true,
		version = "0.0.1-SNAPSHOT",
		description = "Local-first developer environment diagnostic and project onboarding tool."
)
public class InitioCommand implements Runnable {

	@Override
	public void run() {
		CommandLine.usage(this, System.out);
	}
}
