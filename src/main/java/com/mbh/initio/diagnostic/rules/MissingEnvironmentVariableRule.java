package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.diagnostic.DiagnosticRule;
import com.mbh.initio.diagnostic.DiagnosticRuleId;
import com.mbh.initio.diagnostic.EnvironmentRequirementEvaluator;
import com.mbh.initio.diagnostic.EnvironmentRequirementEvaluator.Outcome;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.EnvironmentVariableRequirement;

import java.util.ArrayList;
import java.util.List;

public final class MissingEnvironmentVariableRule implements DiagnosticRule {

	@Override
	public List<DiagnosticIssue> evaluate(AnalysisContext context) {
		List<DiagnosticIssue> issues = new ArrayList<>();
		for (EnvironmentVariableRequirement requirement : context.project().environmentVariableRequirements()) {
			Outcome outcome = EnvironmentRequirementEvaluator.outcome(
					context.local().environmentVariableStatus(requirement.name())
			);
			if (outcome == Outcome.MISSING) {
				issues.add(DiagnosticIssue.forEnvironment(
						DiagnosticRuleId.MISSING_ENVIRONMENT_VARIABLE,
						DiagnosticSeverity.ERROR,
						requirement.name() + " is missing",
						"Add " + requirement.name() + " to .env (expected from " + requirement.source().file() + ")",
						requirement.name(),
						requirement.source().file()
				));
				continue;
			}
			if (outcome == Outcome.EMPTY) {
				issues.add(DiagnosticIssue.forEnvironment(
						DiagnosticRuleId.MISSING_ENVIRONMENT_VARIABLE,
						DiagnosticSeverity.ERROR,
						requirement.name() + " is empty",
						"Set a value for " + requirement.name() + " in .env or your environment",
						requirement.name(),
						requirement.source().file()
				));
			}
		}
		return issues;
	}
}
