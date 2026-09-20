package com.mbh.initio.cli.format;

import com.mbh.initio.model.CiExpectation;
import com.mbh.initio.model.CommandCategory;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectCommand;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.TechnologyCategory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class InfoReportFormatter {

	private static final int MAX_COMMAND_SUMMARY = 6;
	private static final int MAX_CI_RUN_COMMANDS = 3;
	private static final List<CommandCategory> COMMAND_PRIORITY = List.of(
			CommandCategory.RUN,
			CommandCategory.DEV,
			CommandCategory.TEST,
			CommandCategory.BUILD,
			CommandCategory.OTHER
	);

	public void write(ProjectAnalysis analysis, PrintWriter out) {
		out.println("Project");
		out.println(analysis.metadata().name());
		out.println("Path: " + analysis.projectPath());

		writeLanguagesSection(out, analysis);
		writeSection(out, "Frameworks", analysis.technologies(TechnologyCategory.FRAMEWORK));
		writeSection(out, "Build tools", analysis.technologies(TechnologyCategory.BUILD_TOOL));
		writeSection(out, "Package managers", analysis.technologies(TechnologyCategory.PACKAGE_MANAGER));
		writeSection(out, "Tools", analysis.technologies(TechnologyCategory.TOOL));
		writeComposeServices(out, analysis);
		writeCiSection(out, analysis);
		writeCommandsSummary(out, analysis);
	}

	private static void writeLanguagesSection(PrintWriter out, ProjectAnalysis analysis) {
		List<DetectedTechnology> languages = analysis.technologies(TechnologyCategory.LANGUAGE);
		if (languages.isEmpty()) {
			return;
		}
		ReportLayout.blank(out);
		out.println("Languages");
		for (DetectedTechnology language : languages) {
			String version = declaredVersion(analysis, language.name());
			out.println(version == null ? language.name() : language.name() + " " + version);
		}
	}

	private static String declaredVersion(ProjectAnalysis analysis, String languageName) {
		for (RuntimeRequirement requirement : analysis.runtimeRequirements()) {
			if (requirement.runtime().equalsIgnoreCase(languageName)
					&& requirement.requiredVersion() != null
					&& !requirement.requiredVersion().isBlank()) {
				return requirement.requiredVersion();
			}
		}
		return null;
	}

	private static void writeComposeServices(PrintWriter out, ProjectAnalysis analysis) {
		List<ServiceRequirement> services = analysis.serviceRequirements();
		if (services.isEmpty()) {
			return;
		}
		ReportLayout.blank(out);
		out.println("Compose services");
		Set<String> names = new LinkedHashSet<>();
		for (ServiceRequirement service : services) {
			names.add(service.serviceName());
		}
		for (String name : names) {
			out.println(name);
		}
	}

	private static void writeCiSection(PrintWriter out, ProjectAnalysis analysis) {
		List<CiExpectation> expectations = analysis.ciExpectations();
		if (expectations.isEmpty()) {
			return;
		}
		ReportLayout.blank(out);
		out.println("CI");
		for (CiExpectation expectation : expectations) {
			out.println(expectation.workflowFile());
			for (String javaVersion : expectation.javaVersions()) {
				out.println("Java " + javaVersion);
			}
			for (String nodeVersion : expectation.nodeVersions()) {
				out.println("Node " + nodeVersion);
			}
			List<String> runCommands = expectation.runCommands();
			for (int index = 0; index < Math.min(runCommands.size(), MAX_CI_RUN_COMMANDS); index++) {
				out.println(runCommands.get(index));
			}
		}
	}

	private static void writeCommandsSummary(PrintWriter out, ProjectAnalysis analysis) {
		List<ProjectCommand> commands = commandSummary(analysis.projectCommands());
		if (commands.isEmpty()) {
			return;
		}
		ReportLayout.blank(out);
		out.println("Commands");
		for (ProjectCommand command : commands) {
			out.println(command.command());
		}
		out.println("Run initio commands for the full list.");
	}

	private static List<ProjectCommand> commandSummary(List<ProjectCommand> commands) {
		if (commands.isEmpty()) {
			return List.of();
		}
		List<ProjectCommand> summary = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();
		for (CommandCategory category : COMMAND_PRIORITY) {
			for (ProjectCommand command : commands) {
				if (command.category() != category || seen.contains(command.command())) {
					continue;
				}
				summary.add(command);
				seen.add(command.command());
				if (summary.size() >= MAX_COMMAND_SUMMARY) {
					return List.copyOf(summary);
				}
			}
		}
		return List.copyOf(summary);
	}

	private static void writeSection(PrintWriter out, String title, List<DetectedTechnology> technologies) {
		if (technologies.isEmpty()) {
			return;
		}
		ReportLayout.blank(out);
		out.println(title);
		for (DetectedTechnology technology : technologies) {
			out.println(technology.name());
		}
	}
}
