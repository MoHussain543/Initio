package com.mbh.initio.model;

import java.util.Objects;

public record DiagnosticIssue(
		DiagnosticSeverity severity,
		String title,
		String detail
) {
	public DiagnosticIssue {
		Objects.requireNonNull(severity, "severity");
		Objects.requireNonNull(title, "title");
		Objects.requireNonNull(detail, "detail");
	}
}
