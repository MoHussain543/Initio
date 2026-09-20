package com.mbh.initio.cli;

import com.mbh.initio.web.DashboardLauncher;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
		name = "dashboard",
		mixinStandardHelpOptions = true,
		description = {
				"Start the local web dashboard for one project (this server instance).",
				"Defaults: host 127.0.0.1, port 7331.",
				"Examples:",
				"  initio dashboard",
				"  initio dashboard /path/to/repo",
				"  initio dashboard --port 7332"
		}
)
public class DashboardCommand implements Callable<Integer> {

	private static final int DEFAULT_PORT = 7331;
	private static final String DEFAULT_HOST = "127.0.0.1";

	@Spec
	private CommandSpec spec;

	@Parameters(
			index = "0",
			arity = "0..1",
			description = "Project directory (default: current directory)"
	)
	private Path path;

	@Option(
			names = "--port",
			description = "HTTP port for the dashboard (default: ${DEFAULT-VALUE})"
	)
	private int port = DEFAULT_PORT;

	@Option(
			names = "--host",
			description = "Bind address for the dashboard (default: ${DEFAULT-VALUE})"
	)
	private String host = DEFAULT_HOST;

	@Override
	public Integer call() {
		try {
			Path projectPath = ProjectPathResolver.resolve(path);
			return new DashboardLauncher().start(
					projectPath,
					host,
					port,
					spec.commandLine().getOut(),
					spec.commandLine().getErr()
			);
		} catch (IllegalArgumentException exception) {
			spec.commandLine().getErr().println(exception.getMessage());
			return 1;
		}
	}
}
