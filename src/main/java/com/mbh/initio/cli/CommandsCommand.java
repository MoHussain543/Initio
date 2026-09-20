package com.mbh.initio.cli;

import com.mbh.initio.analysis.ProjectAnalysisEnricher;
import com.mbh.initio.analysis.ProjectAnalyzer;
import com.mbh.initio.analysis.ProjectAnalyzers;
import com.mbh.initio.cli.format.CommandsReportFormatter;
import com.mbh.initio.detector.DetectionException;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.projectconfig.InitioConfigException;
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
	private final ProjectAnalysisEnricher projectAnalysisEnricher;

	public CommandsCommand() {
		this(ProjectAnalyzers.create(), new ProjectAnalysisEnricher());
	}

	CommandsCommand(ProjectAnalyzer projectAnalyzer) {
		this(projectAnalyzer, new ProjectAnalysisEnricher());
	}

	CommandsCommand(ProjectAnalyzer projectAnalyzer, ProjectAnalysisEnricher projectAnalysisEnricher) {
		this.projectAnalyzer = projectAnalyzer;
		this.projectAnalysisEnricher = projectAnalysisEnricher;
	}

	@Override
	public Integer call() {
		try {
			Path projectPath = ProjectPathResolver.resolve(path);
			ProjectAnalysis detected = projectAnalyzer.analyze(projectPath);
			ProjectAnalysis analysis = projectAnalysisEnricher.enrich(detected).project();
			new CommandsReportFormatter().write(analysis, spec.commandLine().getOut());
			return 0;
		} catch (IllegalArgumentException | DetectionException | InitioConfigException exception) {
			spec.commandLine().getErr().println(exception.getMessage());
			return 1;
		}
	}
}
