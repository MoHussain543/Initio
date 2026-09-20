package com.mbh.initio.cli.format;

import com.mbh.initio.analysis.AnalysisResult;
import com.mbh.initio.diagnostic.EnvironmentRequirementEvaluator;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator.Outcome;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.RuntimeRequirement;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class CheckReportFormatter {

	public void write(AnalysisResult result, PrintWriter out) {
		ProjectAnalysis project = result.project();
		out.println("INITIO");
		ReportLayout.blank(out);
		out.println("Project: " + project.metadata().name());
		out.println("Path: " + project.projectPath());
		ReportLayout.blank(out);

		ReportLayout.section(out, "Detected stack");
		for (DetectedTechnology technology : ordered(project.technologies())) {
			out.println(technology.name());
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Declared requirements");
		for (RuntimeRequirement requirement : project.runtimeRequirements()) {
			out.println(formatDeclaredRequirement(requirement));
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Runtime");
		if (project.runtimeRequirements().isEmpty()) {
			out.println("No runtime requirements declared");
		} else {
			for (RuntimeRequirement requirement : project.runtimeRequirements()) {
				out.println(formatRuntimeCheck(requirement, result));
			}
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Environment");
		if (project.environmentVariableRequirements().isEmpty()) {
			out.println("No environment variables declared");
		} else {
			for (EnvironmentVariableRequirement requirement : project.environmentVariableRequirements()) {
				out.println(formatEnvironmentCheck(requirement, result));
			}
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Result");
		out.println("Project readiness: " + result.readiness().percent() + "%");
		out.println(result.readiness().summary());
	}

	private static String formatRuntimeCheck(RuntimeRequirement requirement, AnalysisResult result) {
		Optional<InstalledRuntime> installed = result.local().installedRuntime(requirement.runtime());
		Outcome outcome = RuntimeRequirementEvaluator.outcome(requirement, installed);
		String label = formatDeclaredRequirement(requirement);
		return switch (outcome) {
			case UNVERIFIED -> label + " — Could not verify";
			case MISSING -> "✗ " + label + " — not installed";
			case INCOMPATIBLE -> "✗ " + label + " — incompatible (installed "
					+ installed.map(InstalledRuntime::detectedVersion).orElse("unknown") + ")";
			case SATISFIED -> "✓ " + label + " — installed ("
					+ installed.map(InstalledRuntime::detectedVersion).orElse("unknown") + ")";
		};
	}

	private static String formatEnvironmentCheck(EnvironmentVariableRequirement requirement, AnalysisResult result) {
		EnvironmentRequirementEvaluator.Outcome outcome = EnvironmentRequirementEvaluator.outcome(
				result.local().environmentVariableStatus(requirement.name())
		);
		return switch (outcome) {
			case UNVERIFIED -> requirement.name() + " — Could not verify";
			case MISSING -> "✗ " + requirement.name() + " — missing";
			case EMPTY -> "✗ " + requirement.name() + " — empty";
			case SATISFIED -> "✓ " + requirement.name() + " — present";
		};
	}

	private static List<DetectedTechnology> ordered(List<DetectedTechnology> technologies) {
		return technologies.stream()
				.sorted(Comparator
						.comparing((DetectedTechnology technology) -> technology.category().ordinal())
						.thenComparing(DetectedTechnology::name))
				.toList();
	}

	private static String formatDeclaredRequirement(RuntimeRequirement requirement) {
		String label = capitalize(requirement.runtime());
		if (requirement.requiredVersion() == null || requirement.requiredVersion().isBlank()) {
			return label;
		}
		return label + " " + requirement.requiredVersion();
	}

	private static String capitalize(String value) {
		if (value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
