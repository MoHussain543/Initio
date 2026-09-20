package com.mbh.initio.diagnostic;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.ReadinessScore;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.ServiceStatus;
import com.mbh.initio.projectconfig.DiagnosticSuppression;

import java.util.List;
import java.util.Optional;

public final class ReadinessCalculator {

	public ReadinessScore calculate(AnalysisContext context, List<DiagnosticIssue> issues) {
		return calculate(context, issues, List.of());
	}

	public ReadinessScore calculate(
			AnalysisContext context,
			List<DiagnosticIssue> issues,
			List<DiagnosticSuppression> suppressions
	) {
		int verifiedPassed = 0;
		int verifiedTotal = 0;
		int unverifiedCount = 0;

		for (RuntimeRequirement requirement : context.project().runtimeRequirements()) {
			if (requirement.requiredVersion() == null || requirement.requiredVersion().isBlank()) {
				continue;
			}
			RuntimeRequirementEvaluator.Outcome outcome = RuntimeRequirementEvaluator.outcome(
					requirement,
					context.local().installedRuntime(requirement.runtime())
			);
			if (outcome == RuntimeRequirementEvaluator.Outcome.UNVERIFIED) {
				unverifiedCount++;
				continue;
			}
			verifiedTotal++;
			if (outcome == RuntimeRequirementEvaluator.Outcome.SATISFIED) {
				verifiedPassed++;
			}
		}

		for (EnvironmentVariableRequirement requirement : context.project().environmentVariableRequirements()) {
			if (DiagnosticSuppressionFilter.environmentSuppressed(requirement.name(), suppressions)) {
				continue;
			}
			EnvironmentRequirementEvaluator.Outcome outcome = EnvironmentRequirementEvaluator.outcome(
					context.local().environmentVariableStatus(requirement.name())
			);
			if (outcome == EnvironmentRequirementEvaluator.Outcome.UNVERIFIED) {
				unverifiedCount++;
				continue;
			}
			verifiedTotal++;
			if (outcome == EnvironmentRequirementEvaluator.Outcome.SATISFIED) {
				verifiedPassed++;
			}
		}

		for (ServiceRequirement requirement : context.project().serviceRequirements()) {
			ServiceRequirementEvaluator.Outcome outcome = ServiceRequirementEvaluator.outcome(
					context.local().serviceStatus(requirement.serviceName())
			);
			if (outcome == ServiceRequirementEvaluator.Outcome.UNVERIFIED) {
				unverifiedCount++;
				continue;
			}
			verifiedTotal++;
			if (outcome == ServiceRequirementEvaluator.Outcome.RUNNING) {
				verifiedPassed++;
			}
		}

		for (PortExpectation expectation : context.project().portExpectations()) {
			if (expectation.role() != PortRole.APPLICATION) {
				continue;
			}
			if (DiagnosticSuppressionFilter.portSuppressed(expectation.port(), suppressions)) {
				continue;
			}
			PortExpectationEvaluator.Outcome outcome = PortExpectationEvaluator.outcome(
					expectation,
					context.local().portObservation(expectation.port()),
					Optional.<ServiceStatus>empty()
			);
			if (outcome == PortExpectationEvaluator.Outcome.UNVERIFIED) {
				unverifiedCount++;
				continue;
			}
			verifiedTotal++;
			if (outcome == PortExpectationEvaluator.Outcome.AVAILABLE) {
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
			summary.append("Verified: ").append(verifiedPassed).append('/').append(verifiedTotal).append(" requirements");
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
