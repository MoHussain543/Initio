package com.mbh.initio.detector.github;

import com.mbh.initio.detector.YamlDocuments;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class GitHubActionsWorkflowParser {

	private static final List<String> JAVA_MATRIX_KEYS = List.of("java", "java-version", "jdk", "jdk-version");
	private static final List<String> NODE_MATRIX_KEYS = List.of("node", "node-version", "node_version");

	public ParsedWorkflow parse(Path workflowFile) {
		Objects.requireNonNull(workflowFile, "workflowFile");
		Object loaded = YamlDocuments.load(workflowFile);
		Matrix matrix = collectMatrix(loaded);
		ParsedWorkflow.Builder builder = new ParsedWorkflow.Builder();
		walk(loaded, builder, matrix);
		return builder.build();
	}

	private static Matrix collectMatrix(Object document) {
		Set<String> javaVersions = new LinkedHashSet<>();
		Set<String> nodeVersions = new LinkedHashSet<>();
		collectMatrix(document, javaVersions, nodeVersions);
		return new Matrix(List.copyOf(javaVersions), List.copyOf(nodeVersions));
	}

	private static void collectMatrix(Object node, Set<String> javaVersions, Set<String> nodeVersions) {
		if (node instanceof List<?> list) {
			for (Object entry : list) {
				collectMatrix(entry, javaVersions, nodeVersions);
			}
			return;
		}
		if (!(node instanceof Map<?, ?> map)) {
			return;
		}
		Object strategy = map.get("strategy");
		if (strategy instanceof Map<?, ?> strategyMap) {
			Object matrixNode = strategyMap.get("matrix");
			if (matrixNode instanceof Map<?, ?> matrix) {
				for (String key : JAVA_MATRIX_KEYS) {
					javaVersions.addAll(scalarVersions(matrix.get(key)));
				}
				for (String key : NODE_MATRIX_KEYS) {
					nodeVersions.addAll(scalarVersions(matrix.get(key)));
				}
			}
		}
		for (Object value : map.values()) {
			collectMatrix(value, javaVersions, nodeVersions);
		}
	}

	private static List<String> scalarVersions(Object value) {
		if (value == null) {
			return List.of();
		}
		if (value instanceof List<?> list) {
			List<String> versions = new ArrayList<>();
			for (Object entry : list) {
				if (entry == null || entry instanceof Map<?, ?> || entry instanceof List<?>) {
					continue;
				}
				String text = entry.toString().trim();
				if (!text.isEmpty() && !isExpression(text)) {
					versions.add(text);
				}
			}
			return versions;
		}
		if (value instanceof Map<?, ?> || isExpression(String.valueOf(value))) {
			return List.of();
		}
		String text = value.toString().trim();
		return text.isEmpty() ? List.of() : List.of(text);
	}

	private static void walk(Object node, ParsedWorkflow.Builder builder, Matrix matrix) {
		if (node instanceof List<?> list) {
			for (Object entry : list) {
				walk(entry, builder, matrix);
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
					builder.addJavaVersions(extractVersion(withMap.get("java-version"), matrix.java(), JAVA_MATRIX_KEYS));
				}
				if (action.contains("setup-node")) {
					builder.addNodeVersions(extractVersion(withMap.get("node-version"), matrix.node(), NODE_MATRIX_KEYS));
				}
			}
		}
		Object run = map.get("run");
		if (run != null) {
			builder.addRunCommand(normalizeRunCommand(run.toString()));
		}
		for (Object value : map.values()) {
			walk(value, builder, matrix);
		}
	}

	private static List<String> extractVersion(Object value, List<String> matrixValues, List<String> matrixKeys) {
		if (value == null) {
			return List.of();
		}
		if (value instanceof List<?> list) {
			List<String> versions = new ArrayList<>();
			for (Object entry : list) {
				versions.addAll(extractVersion(entry, matrixValues, matrixKeys));
			}
			return versions;
		}
		String text = value.toString().trim();
		if (text.isEmpty()) {
			return List.of();
		}
		if (isExpression(text)) {
			String expression = expressionBody(text);
			if (referencesMatrix(expression, matrixKeys)) {
				return matrixValues;
			}
			return List.of();
		}
		return List.of(text);
	}

	private static boolean referencesMatrix(String expression, List<String> matrixKeys) {
		for (String key : matrixKeys) {
			if (("matrix." + key).equals(expression)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isExpression(String text) {
		return text != null && text.contains("${{") && text.contains("}}");
	}

	private static String expressionBody(String text) {
		int start = text.indexOf("${{");
		int end = text.indexOf("}}", start + 3);
		if (start < 0 || end < 0) {
			return "";
		}
		return text.substring(start + 3, end).trim();
	}

	private record Matrix(List<String> java, List<String> node) {
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
