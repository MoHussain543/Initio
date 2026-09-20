package com.mbh.initio.projectconfig;

import java.util.List;
import java.util.Objects;

public record ConfigValidationResult(List<ConfigIssue> issues) {
	public ConfigValidationResult {
		issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
	}

	public static ConfigValidationResult ok() {
		return new ConfigValidationResult(List.of());
	}

	public boolean valid() {
		return issues.isEmpty();
	}

	public String formattedMessage() {
		if (valid()) {
			return "Configuration valid.";
		}
		StringBuilder text = new StringBuilder("Invalid initio.yml");
		text.append(System.lineSeparator());
		for (ConfigIssue issue : issues) {
			text.append(System.lineSeparator());
			text.append(issue.path()).append(':');
			text.append(System.lineSeparator());
			text.append(issue.message());
			text.append(System.lineSeparator());
		}
		return text.toString().stripTrailing();
	}
}
