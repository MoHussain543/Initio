package com.mbh.initio.cli;

import com.mbh.initio.analysis.AnalysisEngines;
import com.mbh.initio.analysis.AnalysisResult;
import com.mbh.initio.cli.format.DoctorReportFormatter;
import com.mbh.initio.detector.DetectionException;
import com.mbh.initio.projectconfig.InitioConfigException;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
		name = "doctor",
		description = "List environment problems and suggested fixes for this project."
)
public class DoctorCommand implements Callable<Integer> {

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
			new DoctorReportFormatter().write(result, spec.commandLine().getOut());
			return hasErrors(result) ? 1 : 0;
		} catch (IllegalArgumentException | DetectionException | InitioConfigException exception) {
			spec.commandLine().getErr().println(exception.getMessage());
			return 1;
		}
	}

	private static boolean hasErrors(AnalysisResult result) {
		for (DiagnosticIssue issue : result.issues()) {
			if (issue.severity() == DiagnosticSeverity.ERROR) {
				return true;
			}
		}
		return false;
	}
}
