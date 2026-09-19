package com.mbh.Initio.cli.format;

import com.mbh.Initio.model.DetectedTechnology;
import com.mbh.Initio.model.ProjectAnalysis;
import com.mbh.Initio.model.TechnologyCategory;

import java.io.PrintStream;
import java.util.List;

public final class InfoReportFormatter {

	public void write(ProjectAnalysis analysis, PrintStream out) {
		out.println("Project");
		out.println(analysis.metadata().name());

		writeSection(out, "Languages", analysis.technologies(TechnologyCategory.LANGUAGE));
		writeSection(out, "Frameworks", analysis.technologies(TechnologyCategory.FRAMEWORK));
		writeSection(out, "Build tools", analysis.technologies(TechnologyCategory.BUILD_TOOL));
		writeSection(out, "Package managers", analysis.technologies(TechnologyCategory.PACKAGE_MANAGER));
		writeSection(out, "Tools", analysis.technologies(TechnologyCategory.TOOL));
	}

	private static void writeSection(PrintStream out, String title, List<DetectedTechnology> technologies) {
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
