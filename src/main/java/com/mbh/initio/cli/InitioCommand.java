package com.mbh.initio.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
		name = "initio",
		mixinStandardHelpOptions = true,
		versionProvider = InitioVersionProvider.class,
		description = "Local-first developer environment diagnostic and project onboarding tool.",
		subcommands = {
				CheckCommand.class,
				CommandsCommand.class,
				ConfigCommand.class,
				DashboardCommand.class,
				DoctorCommand.class,
				InfoCommand.class
		}
)
public class InitioCommand implements Runnable {

	@Override
	public void run() {
		CommandLine.usage(this, System.out);
	}
}
