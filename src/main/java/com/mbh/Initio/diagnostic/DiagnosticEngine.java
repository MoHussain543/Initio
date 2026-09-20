package com.mbh.initio.diagnostic;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DiagnosticIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DiagnosticEngine {

	private final List<DiagnosticRule> rules;

	public DiagnosticEngine(List<DiagnosticRule> rules) {
		this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
	}

	public List<DiagnosticIssue> evaluate(AnalysisContext context) {
		List<DiagnosticIssue> issues = new ArrayList<>();
		for (DiagnosticRule rule : rules) {
			issues.addAll(rule.evaluate(context));
		}
		return List.copyOf(issues);
	}
}
