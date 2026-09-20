package com.mbh.initio.cli;

import com.mbh.initio.analysis.ProjectAnalyzers;
import com.mbh.initio.analysis.ProjectAnalyzer;
import com.mbh.initio.cli.format.CommandsReportFormatter;
import com.mbh.initio.detector.DetectionException;
import com.mbh.initio.model.ProjectAnalysis;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
		name = "commands",
		description = "List build, test, and run commands detected in this repository."
)
public class CommandsCommand implements Callable<Integer> {

	@Spec
	private CommandSpec spec;

	@Parameters(
			index = "0",
			arity = "0..1",
			description = "Project directory (default: current directory)"
	)
	private Path path;

	private final ProjectAnalyzer projectAnalyzer;

	public CommandsCommand() {
		this(ProjectAnalyzers.create());
	}

	CommandsCommand(ProjectAnalyzer projectAnalyzer) {
		this.projectAnalyzer = projectAnalyzer;
	}

	@Override
	public Integer call() {
		try {
			Path projectPath = ProjectPathResolver.resolve(path);
			ProjectAnalysis analysis = projectAnalyzer.analyze(projectPath);
			new CommandsReportFormatter().write(analysis, spec.commandLine().getOut());
			return 0;
		} catch (IllegalArgumentException | DetectionException exception) {
			spec.commandLine().getErr().println(exception.getMessage());
			return 1;
		}
	}
}
