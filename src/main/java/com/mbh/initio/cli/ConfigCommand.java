package com.mbh.initio.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
		name = "config",
		mixinStandardHelpOptions = true,
		description = "Validate optional initio.yml project configuration.",
		subcommands = {
				ConfigValidateCommand.class
		}
)
public class ConfigCommand implements Runnable {

	@Spec
	private CommandSpec spec;

	@Override
	public void run() {
		spec.commandLine().usage(spec.commandLine().getOut());
	}
}
