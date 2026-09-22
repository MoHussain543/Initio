package com.mbh.initio.cli.format;

import com.mbh.initio.model.CommandCategory;
import com.mbh.initio.model.CommandOrigin;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectCommand;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class TasksReportFormatter {

	private static final List<CommandCategory> CATEGORY_ORDER = List.of(
			CommandCategory.RUN,
			CommandCategory.DEV,
			CommandCategory.TEST,
			CommandCategory.BUILD,
			CommandCategory.OTHER
	);

	public void write(ProjectAnalysis analysis, PrintWriter out) {
		out.println("INITIO");
		ReportLayout.blank(out);
		out.println("Project: " + analysis.metadata().name());
		out.println("Path: " + analysis.projectPath());
		ReportLayout.blank(out);

		List<ProjectCommand> commands = analysis.projectCommands();
		if (commands.isEmpty()) {
			ReportLayout.section(out, "Tasks");
			out.println("No tasks detected");
			return;
		}

		Map<CommandCategory, List<ProjectCommand>> byCategory = groupByCategory(commands);
		for (CommandCategory category : CATEGORY_ORDER) {
			List<ProjectCommand> categoryCommands = byCategory.get(category);
			if (categoryCommands == null || categoryCommands.isEmpty()) {
				continue;
			}
			ReportLayout.section(out, category.name());
			for (ProjectCommand command : categoryCommands) {
				out.println(command.command());
				out.println("Source: " + command.source().file() + " · " + formatOrigin(command.origin()));
			}
			ReportLayout.blank(out);
		}
	}

	private static Map<CommandCategory, List<ProjectCommand>> groupByCategory(List<ProjectCommand> commands) {
		Map<CommandCategory, List<ProjectCommand>> grouped = new EnumMap<>(CommandCategory.class);
		List<ProjectCommand> ordered = commands.stream()
				.sorted(Comparator
						.comparing((ProjectCommand command) -> command.category().ordinal())
						.thenComparing(ProjectCommand::command))
				.toList();
		for (ProjectCommand command : ordered) {
			grouped.computeIfAbsent(command.category(), ignored -> new java.util.ArrayList<>()).add(command);
		}
		return grouped;
	}

	private static String formatOrigin(CommandOrigin origin) {
		return switch (origin) {
			case DECLARED -> "Declared";
			case CONVENTIONAL -> "Suggested (Maven convention)";
			case INFERRED -> "Inferred";
			case CONFIGURED -> "Configured";
		};
	}
}
