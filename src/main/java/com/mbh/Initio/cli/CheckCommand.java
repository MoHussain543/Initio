package com.mbh.initio.cli;

import com.mbh.initio.analysis.AnalysisEngines;
import com.mbh.initio.analysis.AnalysisResult;
import com.mbh.initio.cli.format.CheckReportFormatter;
import com.mbh.initio.detector.DetectionException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
		name = "check",
		description = "Analyze the repository and show declared requirements."
)
public class CheckCommand implements Callable<Integer> {

	@Spec
	private CommandSpec spec;

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
			AnalysisResult result = AnalysisEngines.createDefault().run(projectPath);
			new CheckReportFormatter().write(result, spec.commandLine().getOut());
			return 0;
		} catch (IllegalArgumentException | DetectionException exception) {
			spec.commandLine().getErr().println(exception.getMessage());
			return 1;
		}
	}
}
