package com.mbh.initio.detector.github;

import com.mbh.initio.detector.DetectionException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class GitHubActionsWorkflowParser {

	public ParsedWorkflow parse(Path workflowFile) {
		Objects.requireNonNull(workflowFile, "workflowFile");
		try {
			Object loaded = new Yaml().load(Files.readString(workflowFile));
			ParsedWorkflow.Builder builder = new ParsedWorkflow.Builder();
			walk(loaded, builder);
			return builder.build();
		} catch (IOException exception) {
			throw new DetectionException("Unable to read " + workflowFile.getFileName() + ".", exception);
		}
	}

	private static void walk(Object node, ParsedWorkflow.Builder builder) {
		if (node instanceof List<?> list) {
			for (Object entry : list) {
				walk(entry, builder);
			}
			return;
		}
		if (!(node instanceof Map<?, ?> map)) {
			return;
		}
		Object uses = map.get("uses");
		if (uses != null) {
			String action = uses.toString();
			Object with = map.get("with");
			if (with instanceof Map<?, ?> withMap) {
				if (action.contains("setup-java")) {
					builder.addJavaVersions(extractVersion(withMap.get("java-version")));
				}
				if (action.contains("setup-node")) {
					builder.addNodeVersions(extractVersion(withMap.get("node-version")));
				}
			}
		}
		Object run = map.get("run");
		if (run != null) {
			builder.addRunCommand(normalizeRunCommand(run.toString()));
		}
		for (Object value : map.values()) {
			walk(value, builder);
		}
	}

	private static List<String> extractVersion(Object value) {
		if (value == null) {
			return List.of();
		}
		if (value instanceof List<?> list) {
			List<String> versions = new ArrayList<>();
			for (Object entry : list) {
				versions.addAll(extractVersion(entry));
			}
			return versions;
		}
		String text = value.toString().trim();
		if (text.isEmpty()) {
			return List.of();
		}
		return List.of(text);
	}

	private static String normalizeRunCommand(String command) {
		return command.lines()
				.map(String::trim)
				.filter(line -> !line.isEmpty())
				.reduce((first, second) -> first + " && " + second)
				.orElse("")
				.trim();
	}

	public record ParsedWorkflow(List<String> javaVersions, List<String> nodeVersions, List<String> runCommands) {
		static final class Builder {
			private final Set<String> javaVersions = new LinkedHashSet<>();
			private final Set<String> nodeVersions = new LinkedHashSet<>();
			private final Set<String> runCommands = new LinkedHashSet<>();

			void addJavaVersions(List<String> versions) {
				javaVersions.addAll(versions);
			}

			void addNodeVersions(List<String> versions) {
				nodeVersions.addAll(versions);
			}

			void addRunCommand(String command) {
				if (!command.isBlank()) {
					runCommands.add(command);
				}
			}

			ParsedWorkflow build() {
				return new ParsedWorkflow(
						List.copyOf(javaVersions),
						List.copyOf(nodeVersions),
						List.copyOf(runCommands)
				);
			}
		}
	}
}
