package com.mbh.Initio.cli.format;

import com.mbh.Initio.model.DetectedTechnology;
import com.mbh.Initio.model.ProjectAnalysis;
import com.mbh.Initio.model.RuntimeRequirement;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;

public final class CheckReportFormatter {

	public void write(ProjectAnalysis analysis, PrintWriter out) {
		out.println("INITIO");
		ReportLayout.blank(out);
		out.println("Project: " + analysis.metadata().name());
		out.println("Path: " + analysis.projectPath());
		ReportLayout.blank(out);

		ReportLayout.section(out, "Detected stack");
		for (DetectedTechnology technology : ordered(analysis.technologies())) {
			out.println(technology.name());
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Declared requirements");
		for (RuntimeRequirement requirement : analysis.runtimeRequirements()) {
			out.println(formatRequirement(requirement));
		}
		ReportLayout.blank(out);

		out.println("Local environment: not checked");
	}

	private static List<DetectedTechnology> ordered(List<DetectedTechnology> technologies) {
		return technologies.stream()
				.sorted(Comparator
						.comparing((DetectedTechnology technology) -> technology.category().ordinal())
						.thenComparing(DetectedTechnology::name))
				.toList();
	}

	private static String formatRequirement(RuntimeRequirement requirement) {
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
