package com.mbh.Initio.cli;

import com.mbh.Initio.analysis.ProjectAnalyzers;
import com.mbh.Initio.cli.format.CheckReportFormatter;
import com.mbh.Initio.detector.DetectionException;
import com.mbh.Initio.model.ProjectAnalysis;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
		name = "check",
		description = "Analyze the repository and show declared requirements."
)
public class CheckCommand implements Callable<Integer> {

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
			ProjectAnalysis analysis = ProjectAnalyzers.create().analyze(projectPath);
			new CheckReportFormatter().write(analysis, System.out);
			return 0;
		} catch (IllegalArgumentException | DetectionException exception) {
			System.err.println(exception.getMessage());
			return 1;
		}
	}
}
