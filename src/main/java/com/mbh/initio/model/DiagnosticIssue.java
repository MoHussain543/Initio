package com.mbh.initio.model;

import com.mbh.initio.diagnostic.DiagnosticRuleId;

import java.util.Objects;

public record DiagnosticIssue(
		DiagnosticRuleId ruleId,
		DiagnosticSeverity severity,
		String title,
		String detail,
		String environmentName,
		Integer port
) {
	public DiagnosticIssue {
		Objects.requireNonNull(ruleId, "ruleId");
		Objects.requireNonNull(severity, "severity");
		Objects.requireNonNull(title, "title");
		Objects.requireNonNull(detail, "detail");
		if (environmentName != null && environmentName.isBlank()) {
			environmentName = null;
		}
	}

	public DiagnosticIssue(DiagnosticRuleId ruleId, DiagnosticSeverity severity, String title, String detail) {
		this(ruleId, severity, title, detail, null, null);
	}

	public static DiagnosticIssue forEnvironment(
			DiagnosticRuleId ruleId,
			DiagnosticSeverity severity,
			String title,
			String detail,
			String environmentName
	) {
		return new DiagnosticIssue(
				ruleId,
				severity,
				title,
				detail,
				Objects.requireNonNull(environmentName, "environmentName"),
				null
		);
	}

	public static DiagnosticIssue forPort(
			DiagnosticRuleId ruleId,
			DiagnosticSeverity severity,
			String title,
			String detail,
			int port
	) {
		return new DiagnosticIssue(ruleId, severity, title, detail, null, port);
	}
}
