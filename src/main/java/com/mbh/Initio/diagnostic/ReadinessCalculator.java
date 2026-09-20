package com.mbh.initio.diagnostic;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator.Outcome;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.ReadinessScore;
import com.mbh.initio.model.RuntimeRequirement;

import java.util.List;

public final class ReadinessCalculator {

	public ReadinessScore calculate(AnalysisContext context, List<DiagnosticIssue> issues) {
		int verifiedPassed = 0;
		int verifiedTotal = 0;
		int unverifiedCount = 0;

		for (RuntimeRequirement requirement : context.project().runtimeRequirements()) {
			if (requirement.requiredVersion() == null || requirement.requiredVersion().isBlank()) {
				continue;
			}
			Outcome outcome = RuntimeRequirementEvaluator.outcome(
					requirement,
					context.local().installedRuntime(requirement.runtime())
			);
			if (outcome == Outcome.UNVERIFIED) {
				unverifiedCount++;
				continue;
			}
			verifiedTotal++;
			if (outcome == Outcome.SATISFIED) {
				verifiedPassed++;
			}
		}

		int percent = verifiedTotal == 0 ? 100 : (verifiedPassed * 100) / verifiedTotal;
		String summary = buildSummary(verifiedPassed, verifiedTotal, unverifiedCount, issues.size());
		return new ReadinessScore(percent, verifiedPassed, verifiedTotal, unverifiedCount, summary);
	}

	private static String buildSummary(int verifiedPassed, int verifiedTotal, int unverifiedCount, int issueCount) {
		StringBuilder summary = new StringBuilder();
		if (verifiedTotal == 0) {
			summary.append("Verified: none");
		} else {
			summary.append("Verified: ").append(verifiedPassed).append('/').append(verifiedTotal).append(" runtime requirements");
		}
		if (unverifiedCount > 0) {
			summary.append(" · ").append(unverifiedCount).append(" could not verify");
		}
		if (issueCount > 0) {
			summary.append(" · ").append(issueCount).append(" issues");
		}
		return summary.toString();
	}
}
