package com.mbh.initio.projectconfig;

import com.mbh.initio.diagnostic.DiagnosticRuleId;
import com.mbh.initio.model.CommandCategory;
import com.mbh.initio.system.VersionMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class InitioConfigValidator {

	private static final Set<String> ROOT_KEYS = Set.of(
			"environment",
			"runtimes",
			"commands",
			"services",
			"ignore"
	);
	private static final Set<String> COMMAND_KEYS = Set.of("name", "category", "command");
	private static final Set<String> SERVICE_KEYS = Set.of("name", "port");
	private static final Set<String> ENVIRONMENT_KEYS = Set.of("required");
	private static final String ALLOWED_CATEGORIES = Arrays.stream(CommandCategory.values())
			.map(Enum::name)
			.collect(Collectors.joining(System.lineSeparator()));
	private static final String ALLOWED_RULES = Arrays.stream(DiagnosticRuleId.values())
			.map(Enum::name)
			.collect(Collectors.joining(System.lineSeparator()));

	public ConfigParseResult parse(Object root) {
		List<ConfigIssue> issues = new ArrayList<>();
		if (root == null) {
			return ConfigParseResult.valid(InitioProjectConfig.empty());
		}
		if (!(root instanceof Map<?, ?> map)) {
			issues.add(new ConfigIssue("initio.yml", "The file must be a YAML mapping."));
			return ConfigParseResult.invalid(new ConfigValidationResult(issues));
		}
		rejectUnknownKeys(map, ROOT_KEYS, "", issues);

		List<String> environment = parseEnvironment(map.get("environment"), issues);
		Map<String, String> runtimes = parseRuntimes(map.get("runtimes"), issues);
		List<ConfiguredCommand> commands = parseCommands(map.get("commands"), issues);
		List<ConfiguredService> services = parseServices(map.get("services"), issues);
		List<DiagnosticSuppression> suppressions = parseIgnore(map.get("ignore"), issues);

		if (!issues.isEmpty()) {
			return ConfigParseResult.invalid(new ConfigValidationResult(issues));
		}
		return ConfigParseResult.valid(new InitioProjectConfig(
				environment,
				runtimes,
				commands,
				services,
				suppressions
		));
	}

	private static List<String> parseEnvironment(Object node, List<ConfigIssue> issues) {
		if (node == null) {
			return List.of();
		}
		if (!(node instanceof Map<?, ?> map)) {
			issues.add(new ConfigIssue("environment", "Must be a mapping with a required list."));
			return List.of();
		}
		rejectUnknownKeys(map, ENVIRONMENT_KEYS, "environment", issues);
		Object required = map.get("required");
		if (required == null) {
			return List.of();
		}
		if (!(required instanceof List<?> entries)) {
			issues.add(new ConfigIssue("environment.required", "Must be a list of environment variable names."));
			return List.of();
		}
		List<String> names = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();
		for (int index = 0; index < entries.size(); index++) {
			String path = "environment.required[" + index + "]";
			String name = environmentName(entries.get(index), path, issues);
			if (name == null) {
				continue;
			}
			if (!seen.add(name)) {
				issues.add(new ConfigIssue(path, "Duplicate environment variable \"" + name + "\"."));
				continue;
			}
			names.add(name);
		}
		return names;
	}

	private static Map<String, String> parseRuntimes(Object node, List<ConfigIssue> issues) {
		if (node == null) {
			return Map.of();
		}
		if (!(node instanceof Map<?, ?> map)) {
			issues.add(new ConfigIssue("runtimes", "Must be a mapping of runtime name to version."));
			return Map.of();
		}
		Map<String, String> runtimes = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			String runtime = text(entry.getKey());
			String path = "runtimes";
			if (runtime == null) {
				issues.add(new ConfigIssue(path, "Runtime names must be non-empty strings."));
				continue;
			}
			path = "runtimes." + runtime;
			String version = runtimeConstraint(entry.getValue(), path, issues);
			if (version == null) {
				continue;
			}
			runtimes.put(runtime, version);
		}
		return runtimes;
	}

	private static List<ConfiguredCommand> parseCommands(Object node, List<ConfigIssue> issues) {
		if (node == null) {
			return List.of();
		}
		if (!(node instanceof List<?> entries)) {
			issues.add(new ConfigIssue("commands", "Must be a list of command mappings."));
			return List.of();
		}
		List<ConfiguredCommand> commands = new ArrayList<>();
		Set<String> seenNames = new LinkedHashSet<>();
		for (int index = 0; index < entries.size(); index++) {
			String path = "commands[" + index + "]";
			Object entry = entries.get(index);
			if (!(entry instanceof Map<?, ?> map)) {
				issues.add(new ConfigIssue(path, "Must be a mapping with name, category, and command."));
				continue;
			}
			rejectUnknownKeys(map, COMMAND_KEYS, path, issues);
			String name = requiredString(map.get("name"), path + ".name", "Command name", issues);
			CommandCategory category = parseCategory(map.get("category"), path + ".category", issues);
			String command = requiredString(map.get("command"), path + ".command", "Command", issues);
			if (name == null || category == null || command == null) {
				continue;
			}
			if (!seenNames.add(name)) {
				issues.add(new ConfigIssue(path + ".name", "Duplicate command name \"" + name + "\"."));
				continue;
			}
			commands.add(new ConfiguredCommand(name, category, command));
		}
		return commands;
	}

	private static List<ConfiguredService> parseServices(Object node, List<ConfigIssue> issues) {
		if (node == null) {
			return List.of();
		}
		if (!(node instanceof List<?> entries)) {
			issues.add(new ConfigIssue("services", "Must be a list of service mappings."));
			return List.of();
		}
		List<ConfiguredService> services = new ArrayList<>();
		Set<String> seenNames = new LinkedHashSet<>();
		for (int index = 0; index < entries.size(); index++) {
			String path = "services[" + index + "]";
			Object entry = entries.get(index);
			if (!(entry instanceof Map<?, ?> map)) {
				issues.add(new ConfigIssue(path, "Must be a mapping with name and port."));
				continue;
			}
			rejectUnknownKeys(map, SERVICE_KEYS, path, issues);
			String name = requiredString(map.get("name"), path + ".name", "Service name", issues);
			Integer port = parsePort(map.get("port"), path + ".port", issues);
			if (name == null || port == null) {
				continue;
			}
			if (!seenNames.add(name)) {
				issues.add(new ConfigIssue(path + ".name", "Duplicate service name \"" + name + "\"."));
				continue;
			}
			services.add(new ConfiguredService(name, port));
		}
		return services;
	}

	private static List<DiagnosticSuppression> parseIgnore(Object node, List<ConfigIssue> issues) {
		if (node == null) {
			return List.of();
		}
		if (!(node instanceof List<?> entries)) {
			issues.add(new ConfigIssue("ignore", "Must be a list of suppression expressions."));
			return List.of();
		}
		List<DiagnosticSuppression> suppressions = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();
		for (int index = 0; index < entries.size(); index++) {
			String path = "ignore[" + index + "]";
			String expression = requiredString(entries.get(index), path, "Suppression", issues);
			if (expression == null) {
				continue;
			}
			DiagnosticSuppression suppression = parseSuppression(expression, path, issues);
			if (suppression == null) {
				continue;
			}
			String key = suppressionKey(suppression);
			if (!seen.add(key)) {
				issues.add(new ConfigIssue(path, "Duplicate suppression \"" + expression + "\"."));
				continue;
			}
			suppressions.add(suppression);
		}
		return suppressions;
	}

	private static DiagnosticSuppression parseSuppression(String expression, String path, List<ConfigIssue> issues) {
		int separator = expression.indexOf(':');
		if (separator <= 0 || separator == expression.length() - 1) {
			issues.add(new ConfigIssue(path, unknownIgnoreMessage(expression)));
			return null;
		}
		String prefix = expression.substring(0, separator).trim().toLowerCase();
		String value = expression.substring(separator + 1).trim();
		if (value.isEmpty()) {
			issues.add(new ConfigIssue(path, unknownIgnoreMessage(expression)));
			return null;
		}
		return switch (prefix) {
			case "env" -> {
				if (!isEnvironmentName(value)) {
					issues.add(new ConfigIssue(path, "Environment variable name \"" + value + "\" is not valid."));
					yield null;
				}
				yield new DiagnosticSuppression.Environment(value.toUpperCase(Locale.ROOT));
			}
			case "port" -> {
				Integer port = parsePort(value, path, issues);
				yield port == null ? null : new DiagnosticSuppression.Port(port);
			}
			case "rule" -> {
				DiagnosticRuleId ruleId = parseRuleId(value, path, issues);
				yield ruleId == null ? null : new DiagnosticSuppression.Rule(ruleId);
			}
			default -> {
				issues.add(new ConfigIssue(path, unknownIgnoreMessage(expression)));
				yield null;
			}
		};
	}

	private static String unknownIgnoreMessage(String expression) {
		return "Unknown suppression \"" + expression + "\"."
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "Allowed:"
				+ System.lineSeparator()
				+ "env:<NAME>"
				+ System.lineSeparator()
				+ "port:<number>"
				+ System.lineSeparator()
				+ "rule:<RULE_ID>";
	}

	private static DiagnosticRuleId parseRuleId(String value, String path, List<ConfigIssue> issues) {
		try {
			return DiagnosticRuleId.valueOf(value);
		} catch (IllegalArgumentException exception) {
			issues.add(new ConfigIssue(
					path,
					"Unknown rule \"" + value + "\"."
							+ System.lineSeparator()
							+ System.lineSeparator()
							+ "Allowed:"
							+ System.lineSeparator()
							+ ALLOWED_RULES
			));
			return null;
		}
	}

	private static CommandCategory parseCategory(Object value, String path, List<ConfigIssue> issues) {
		String text = requiredString(value, path, "Command category", issues);
		if (text == null) {
			return null;
		}
		try {
			return CommandCategory.valueOf(text);
		} catch (IllegalArgumentException exception) {
			issues.add(new ConfigIssue(
					path,
					"Unknown category \"" + text + "\"."
							+ System.lineSeparator()
							+ System.lineSeparator()
							+ "Allowed:"
							+ System.lineSeparator()
							+ ALLOWED_CATEGORIES
			));
			return null;
		}
	}

	private static Integer parsePort(Object value, String path, List<ConfigIssue> issues) {
		if (value == null) {
			issues.add(new ConfigIssue(path, "Port is required."));
			return null;
		}
		if (value instanceof List<?> || value instanceof Map<?, ?>) {
			issues.add(new ConfigIssue(path, "Port must be an integer between 1 and 65535."));
			return null;
		}
		if (value instanceof Integer integer) {
			return validPort(integer, path, issues);
		}
		if (value instanceof Long longValue) {
			if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
				issues.add(new ConfigIssue(path, "Port " + longValue + " is not valid. Use a value between 1 and 65535."));
				return null;
			}
			return validPort(longValue.intValue(), path, issues);
		}
		if (value instanceof Number) {
			issues.add(new ConfigIssue(path, "Port must be an integer between 1 and 65535."));
			return null;
		}
		if (!(value instanceof String text)) {
			issues.add(new ConfigIssue(path, "Port must be an integer between 1 and 65535."));
			return null;
		}
		String digits = text.trim();
		if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
			issues.add(new ConfigIssue(path, "Port \"" + text + "\" is not an integer."));
			return null;
		}
		try {
			return validPort(Integer.parseInt(digits), path, issues);
		} catch (NumberFormatException exception) {
			issues.add(new ConfigIssue(path, "Port \"" + text + "\" is not an integer."));
			return null;
		}
	}

	private static Integer validPort(int port, String path, List<ConfigIssue> issues) {
		if (port < 1 || port > 65535) {
			issues.add(new ConfigIssue(path, "Port " + port + " is not valid. Use a value between 1 and 65535."));
			return null;
		}
		return port;
	}

	private static String environmentName(Object value, String path, List<ConfigIssue> issues) {
		String name = requiredString(value, path, "Environment variable name", issues);
		if (name == null) {
			return null;
		}
		if (!isEnvironmentName(name)) {
			issues.add(new ConfigIssue(path, "Environment variable name \"" + name + "\" is not valid."));
			return null;
		}
		return name;
	}

	private static String runtimeConstraint(Object value, String path, List<ConfigIssue> issues) {
		if (value instanceof List<?> || value instanceof Map<?, ?>) {
			issues.add(new ConfigIssue(path, "Runtime version must be a string such as 25 or >=22."));
			return null;
		}
		String version = requiredText(value, path, "Runtime version", issues);
		if (version == null) {
			return null;
		}
		if (!VersionMatcher.isSupportedConstraint(version)) {
			issues.add(new ConfigIssue(
					path,
					"Runtime version \"" + version + "\" is not supported. Use a simple constraint such as 25, >=22, 21.x, ^22, or ~22."
			));
			return null;
		}
		return version;
	}

	private static String requiredString(Object value, String path, String label, List<ConfigIssue> issues) {
		if (value instanceof List<?> || value instanceof Map<?, ?>) {
			issues.add(new ConfigIssue(path, label + " must be a string."));
			return null;
		}
		if (value != null && !(value instanceof String)) {
			issues.add(new ConfigIssue(path, label + " must be a string."));
			return null;
		}
		return requiredText(value, path, label, issues);
	}

	private static boolean isEnvironmentName(String name) {
		return name.matches("[A-Za-z_][A-Za-z0-9_]*");
	}

	private static String requiredText(Object value, String path, String label, List<ConfigIssue> issues) {
		String text = text(value);
		if (text == null) {
			issues.add(new ConfigIssue(path, label + " is required."));
			return null;
		}
		return text;
	}

	private static String text(Object value) {
		if (value == null) {
			return null;
		}
		String text = String.valueOf(value).trim();
		return text.isEmpty() ? null : text;
	}

	private static void rejectUnknownKeys(Map<?, ?> map, Set<String> allowed, String path, List<ConfigIssue> issues) {
		for (Object key : map.keySet()) {
			String name = text(key);
			if (name == null || allowed.contains(name)) {
				continue;
			}
			String issuePath = path.isEmpty() ? name : path + "." + name;
			issues.add(new ConfigIssue(issuePath, "Unknown field \"" + name + "\"."));
		}
	}

	private static String suppressionKey(DiagnosticSuppression suppression) {
		return switch (suppression) {
			case DiagnosticSuppression.Environment environment -> "env:" + environment.name();
			case DiagnosticSuppression.Port port -> "port:" + port.port();
			case DiagnosticSuppression.Rule rule -> "rule:" + rule.ruleId().name();
		};
	}

	public record ConfigParseResult(InitioProjectConfig config, ConfigValidationResult validation) {
		public static ConfigParseResult valid(InitioProjectConfig config) {
			return new ConfigParseResult(config, ConfigValidationResult.ok());
		}

		public static ConfigParseResult invalid(ConfigValidationResult validation) {
			return new ConfigParseResult(null, validation);
		}

		public boolean valid() {
			return validation.valid();
		}
	}
}
