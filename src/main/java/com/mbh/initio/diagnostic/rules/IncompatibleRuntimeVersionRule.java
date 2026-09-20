package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.diagnostic.DiagnosticRule;
import com.mbh.initio.diagnostic.DiagnosticRuleId;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator.Outcome;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.RuntimeRequirement;

import java.util.ArrayList;
import java.util.List;

public final class IncompatibleRuntimeVersionRule implements DiagnosticRule {

	@Override
	public List<DiagnosticIssue> evaluate(AnalysisContext context) {
		List<DiagnosticIssue> issues = new ArrayList<>();
		for (RuntimeRequirement requirement : context.project().runtimeRequirements()) {
			if (requirement.requiredVersion() == null || requirement.requiredVersion().isBlank()) {
				continue;
			}
			Outcome outcome = RuntimeRequirementEvaluator.outcome(
					requirement,
					context.local().installedRuntime(requirement.runtime())
			);
			if (outcome != Outcome.INCOMPATIBLE) {
				continue;
			}
			InstalledRuntime installed = context.local().installedRuntime(requirement.runtime()).orElseThrow();
			String runtimeLabel = capitalize(requirement.runtime());
			issues.add(new DiagnosticIssue(
					DiagnosticRuleId.INCOMPATIBLE_RUNTIME,
					DiagnosticSeverity.ERROR,
					"Installed " + runtimeLabel + " does not satisfy project requirement",
					"Required " + requirement.requiredVersion() + " from " + requirement.source().file()
							+ ", found " + installed.detectedVersion(),
					requirement.source().file()
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
