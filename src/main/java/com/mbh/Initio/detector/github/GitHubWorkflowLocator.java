package com.mbh.initio.detector.github;

import com.mbh.initio.analysis.ProjectContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GitHubWorkflowLocator {

	static final Path WORKFLOWS_DIR = Path.of(".github", "workflows");

	public List<Path> locate(ProjectContext context) {
		Path workflowsRoot = context.resolve(WORKFLOWS_DIR.toString());
		if (!Files.isDirectory(workflowsRoot)) {
			return List.of();
		}
		List<Path> workflows = new ArrayList<>();
		try (var stream = Files.list(workflowsRoot)) {
			stream.filter(Files::isRegularFile)
					.filter(path -> isWorkflowFile(path.getFileName().toString()))
					.map(path -> WORKFLOWS_DIR.resolve(path.getFileName()))
					.forEach(workflows::add);
		} catch (IOException exception) {
			return List.of();
		}
		return List.copyOf(workflows);
	}

	private static boolean isWorkflowFile(String filename) {
		String lower = filename.toLowerCase(Locale.ROOT);
		return lower.endsWith(".yml") || lower.endsWith(".yaml");
	}
}
