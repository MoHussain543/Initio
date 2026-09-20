package com.mbh.initio.detector.maven;

import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.model.CommandCategory;
import com.mbh.initio.model.CommandOrigin;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.ProjectCommand;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class MavenConventionalCommands {

	private MavenConventionalCommands() {
	}

	static List<ProjectCommand> conventionalCommands(ProjectContext context, boolean springBoot) {
		String mavenExecutable = mavenExecutable(context);
		DetectionSource source = new DetectionSource(
				Path.of(MavenDetector.POM_FILE),
				"Maven convention",
				DetectionConfidence.MEDIUM
		);
		List<ProjectCommand> commands = new ArrayList<>();
		commands.add(conventional("test", mavenExecutable + " test", CommandCategory.TEST, source));
		commands.add(conventional("package", mavenExecutable + " package", CommandCategory.BUILD, source));
		if (springBoot) {
			commands.add(conventional("spring-boot:run", mavenExecutable + " spring-boot:run", CommandCategory.RUN, source));
		}
		return List.copyOf(commands);
	}

	private static String mavenExecutable(ProjectContext context) {
		if (context.hasFile(MavenDetector.WRAPPER_UNIX)) {
			return "./" + MavenDetector.WRAPPER_UNIX;
		}
		if (context.hasFile(MavenDetector.WRAPPER_WINDOWS)) {
			return MavenDetector.WRAPPER_WINDOWS;
		}
		return "mvn";
	}

	private static ProjectCommand conventional(
			String name,
			String command,
			CommandCategory category,
			DetectionSource source
	) {
		return new ProjectCommand(name, command, category, CommandOrigin.CONVENTIONAL, source);
	}
}
