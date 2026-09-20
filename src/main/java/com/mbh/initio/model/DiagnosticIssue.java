package com.mbh.initio.model;

import com.mbh.initio.diagnostic.DiagnosticRuleId;

import java.nio.file.Path;
import java.util.Objects;

public record DiagnosticIssue(
		DiagnosticRuleId ruleId,
		DiagnosticSeverity severity,
		String title,
		String detail,
		String environmentName,
		Integer port,
		Path sourceFile
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
		this(ruleId, severity, title, detail, null, null, null);
	}

	public DiagnosticIssue(
			DiagnosticRuleId ruleId,
			DiagnosticSeverity severity,
			String title,
			String detail,
			Path sourceFile
	) {
		this(ruleId, severity, title, detail, null, null, sourceFile);
	}

	public static DiagnosticIssue forEnvironment(
			DiagnosticRuleId ruleId,
			DiagnosticSeverity severity,
			String title,
			String detail,
			String environmentName
	) {
		return forEnvironment(ruleId, severity, title, detail, environmentName, null);
	}

	public static DiagnosticIssue forEnvironment(
			DiagnosticRuleId ruleId,
			DiagnosticSeverity severity,
			String title,
			String detail,
			String environmentName,
			Path sourceFile
	) {
		return new DiagnosticIssue(
				ruleId,
				severity,
				title,
				detail,
				Objects.requireNonNull(environmentName, "environmentName"),
				null,
				sourceFile
		);
	}

	public static DiagnosticIssue forPort(
			DiagnosticRuleId ruleId,
			DiagnosticSeverity severity,
			String title,
			String detail,
			int port
	) {
		return forPort(ruleId, severity, title, detail, port, null);
	}

	public static DiagnosticIssue forPort(
			DiagnosticRuleId ruleId,
			DiagnosticSeverity severity,
			String title,
			String detail,
			int port,
			Path sourceFile
	) {
		return new DiagnosticIssue(ruleId, severity, title, detail, null, port, sourceFile);
	}
}
