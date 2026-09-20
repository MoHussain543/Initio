package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.diagnostic.DiagnosticRule;
import com.mbh.initio.diagnostic.DiagnosticRuleId;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator.Outcome;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.RuntimeRequirement;

import java.util.ArrayList;
import java.util.List;

public final class MissingRuntimeRule implements DiagnosticRule {

	@Override
	public List<DiagnosticIssue> evaluate(AnalysisContext context) {
		List<DiagnosticIssue> issues = new ArrayList<>();
		for (RuntimeRequirement requirement : context.project().runtimeRequirements()) {
			Outcome outcome = RuntimeRequirementEvaluator.outcome(
					requirement,
					context.local().installedRuntime(requirement.runtime())
			);
			if (outcome != Outcome.MISSING) {
				continue;
			}
			String runtimeLabel = capitalize(requirement.runtime());
			issues.add(new DiagnosticIssue(
					DiagnosticRuleId.MISSING_RUNTIME,
					DiagnosticSeverity.ERROR,
					runtimeLabel + " is not installed",
					"Install " + runtimeLabel + " to satisfy " + requirement.source().file()
			));
		}
		return issues;
	}

	private static String capitalize(String value) {
		if (value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
