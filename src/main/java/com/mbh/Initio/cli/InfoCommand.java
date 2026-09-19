package com.mbh.Initio.cli;

import com.mbh.Initio.analysis.ProjectAnalyzer;
import com.mbh.Initio.cli.format.InfoReportFormatter;
import com.mbh.Initio.model.ProjectAnalysis;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
		name = "info",
		description = "Show detected project information."
)
public class InfoCommand implements Callable<Integer> {

	@Parameters(
			index = "0",
			arity = "0..1",
			description = "Project directory (default: current directory)"
	)
	private Path path;

	@Override
	public Integer call() {
		try {
			Path projectPath = ProjectPathResolver.resolve(path);
			ProjectAnalysis analysis = new ProjectAnalyzer().analyze(projectPath);
			new InfoReportFormatter().write(analysis, System.out);
			return 0;
		} catch (IllegalArgumentException exception) {
			System.err.println(exception.getMessage());
			return 1;
		}
	}
}
