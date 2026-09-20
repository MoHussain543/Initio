package com.mbh.initio.diagnostic;

import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.projectconfig.DiagnosticSuppression;

import java.util.List;
import java.util.Objects;

public final class DiagnosticSuppressionFilter {

	private DiagnosticSuppressionFilter() {
	}

	public static List<DiagnosticIssue> visible(
			List<DiagnosticIssue> issues,
			List<DiagnosticSuppression> suppressions
	) {
		Objects.requireNonNull(issues, "issues");
		Objects.requireNonNull(suppressions, "suppressions");
		if (suppressions.isEmpty() || issues.isEmpty()) {
			return List.copyOf(issues);
		}
		return issues.stream()
				.filter(issue -> !suppressed(issue, suppressions))
				.toList();
	}

	public static boolean environmentSuppressed(String name, List<DiagnosticSuppression> suppressions) {
		if (name == null || name.isBlank()) {
			return false;
		}
		for (DiagnosticSuppression suppression : suppressions) {
			if (suppression instanceof DiagnosticSuppression.Environment environment
					&& name.equalsIgnoreCase(environment.name())) {
				return true;
			}
		}
		return false;
	}

	public static boolean portSuppressed(int port, List<DiagnosticSuppression> suppressions) {
		for (DiagnosticSuppression suppression : suppressions) {
			if (suppression instanceof DiagnosticSuppression.Port suppressed && suppressed.port() == port) {
				return true;
			}
		}
		return false;
	}

	static boolean suppressed(DiagnosticIssue issue, List<DiagnosticSuppression> suppressions) {
		for (DiagnosticSuppression suppression : suppressions) {
			if (matches(issue, suppression)) {
				return true;
			}
		}
		return false;
	}

	static boolean matches(DiagnosticIssue issue, DiagnosticSuppression suppression) {
		return switch (suppression) {
			case DiagnosticSuppression.Rule rule -> issue.ruleId() == rule.ruleId();
			case DiagnosticSuppression.Environment environment ->
					issue.ruleId() == DiagnosticRuleId.MISSING_ENVIRONMENT_VARIABLE
							&& issue.environmentName() != null
							&& issue.environmentName().equalsIgnoreCase(environment.name());
			case DiagnosticSuppression.Port port ->
					issue.ruleId() == DiagnosticRuleId.PORT_CONFLICT
							&& issue.port() != null
							&& issue.port() == port.port();
		};
	}
}
