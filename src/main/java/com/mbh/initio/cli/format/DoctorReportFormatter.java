package com.mbh.initio.cli.format;

import com.mbh.initio.analysis.AnalysisResult;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.ProjectAnalysis;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;

public final class DoctorReportFormatter {

	public void write(AnalysisResult result, PrintWriter out) {
		ProjectAnalysis project = result.project();
		out.println("INITIO DOCTOR");
		ReportLayout.blank(out);
		out.println("Project: " + project.metadata().name());
		out.println("Path: " + project.projectPath());
		ReportLayout.blank(out);

		List<DiagnosticIssue> issues = orderedIssues(result.issues());
		ReportLayout.section(out, "Issues");
		if (issues.isEmpty()) {
			out.println("No issues found.");
			out.println("Your environment matches the declared requirements.");
			return;
		}

		int index = 1;
		for (DiagnosticIssue issue : issues) {
			out.println(index + ". " + formatSeverity(issue.severity()) + " " + issue.title());
			out.println("   " + issue.detail());
			index++;
		}
	}

	private static List<DiagnosticIssue> orderedIssues(List<DiagnosticIssue> issues) {
		return issues.stream()
				.sorted(Comparator
						.comparing((DiagnosticIssue issue) -> severityRank(issue.severity()))
						.thenComparing(DiagnosticIssue::title))
				.toList();
	}

	private static int severityRank(DiagnosticSeverity severity) {
		return switch (severity) {
			case ERROR -> 0;
			case WARNING -> 1;
			case INFO -> 2;
		};
	}

	private static String formatSeverity(DiagnosticSeverity severity) {
		return switch (severity) {
			case ERROR -> "[ERROR]";
			case WARNING -> "[WARN]";
			case INFO -> "[INFO]";
		};
	}
}
