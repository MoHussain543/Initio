package com.mbh.initio.cli;

import com.mbh.initio.cli.format.ConfigValidateReportFormatter;
import com.mbh.initio.projectconfig.InitioConfigLoadResult;
import com.mbh.initio.projectconfig.InitioConfigLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

@Command(
		name = "validate",
		mixinStandardHelpOptions = true,
		description = "Validate initio.yml. Configuration is optional."
)
public class ConfigValidateCommand implements Callable<Integer> {

	@Spec
	private CommandSpec spec;

	@Parameters(
			index = "0",
			arity = "0..1",
			description = "Project directory (default: current directory)"
	)
	private Path path;

	private final InitioConfigLoader configLoader;

	public ConfigValidateCommand() {
		this(new InitioConfigLoader());
	}

	ConfigValidateCommand(InitioConfigLoader configLoader) {
		this.configLoader = Objects.requireNonNull(configLoader, "configLoader");
	}

	@Override
	public Integer call() {
		try {
			Path projectPath = ProjectPathResolver.resolve(path);
			InitioConfigLoadResult result = configLoader.load(projectPath);
			new ConfigValidateReportFormatter().write(result, spec.commandLine().getOut());
			return result.isInvalid() ? 1 : 0;
		} catch (IllegalArgumentException exception) {
			spec.commandLine().getErr().println(exception.getMessage());
			return 1;
		}
	}
}
