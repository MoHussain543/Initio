package com.mbh.initio.diagnostic;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
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

	static final int WEIGHT_CRITICAL = 3;
	static final int WEIGHT_SECONDARY = 1;

	public ReadinessScore calculate(AnalysisContext context, List<DiagnosticIssue> issues) {
		return calculate(context, issues, List.of());
	}

	public ReadinessScore calculate(
			AnalysisContext context,
			List<DiagnosticIssue> issues,
			List<DiagnosticSuppression> suppressions
	) {
		Tally tally = new Tally();

		for (RuntimeRequirement requirement : context.project().runtimeRequirements()) {
			if (requirement.requiredVersion() == null || requirement.requiredVersion().isBlank()) {
				continue;
			}
			RuntimeRequirementEvaluator.Outcome outcome = RuntimeRequirementEvaluator.outcome(
					requirement,
					context.local().installedRuntime(requirement.runtime())
			);
			switch (outcome) {
				case UNVERIFIED -> tally.unknown();
				case SATISFIED -> tally.pass(WEIGHT_CRITICAL);
				case MISSING -> tally.failUnlessSuppressed(
						WEIGHT_CRITICAL,
						DiagnosticSuppressionFilter.ruleSuppressed(DiagnosticRuleId.MISSING_RUNTIME, suppressions)
				);
				case INCOMPATIBLE -> tally.failUnlessSuppressed(
						WEIGHT_CRITICAL,
						DiagnosticSuppressionFilter.ruleSuppressed(DiagnosticRuleId.INCOMPATIBLE_RUNTIME, suppressions)
				);
			}
		}

		for (EnvironmentVariableRequirement requirement : context.project().environmentVariableRequirements()) {
			if (DiagnosticSuppressionFilter.environmentSuppressed(requirement.name(), suppressions)) {
				continue;
			}
			EnvironmentRequirementEvaluator.Outcome outcome = EnvironmentRequirementEvaluator.outcome(
					context.local().environmentVariableStatus(requirement.name())
			);
			boolean envRuleSuppressed = DiagnosticSuppressionFilter.ruleSuppressed(
					DiagnosticRuleId.MISSING_ENVIRONMENT_VARIABLE,
					suppressions
			);
			switch (outcome) {
				case UNVERIFIED -> tally.unknown();
				case SATISFIED -> tally.pass(WEIGHT_CRITICAL);
				case MISSING, EMPTY -> tally.failUnlessSuppressed(WEIGHT_CRITICAL, envRuleSuppressed);
			}
		}

		for (ServiceRequirement requirement : context.project().serviceRequirements()) {
			ServiceRequirementEvaluator.Outcome outcome = ServiceRequirementEvaluator.outcome(
					context.local().serviceStatus(requirement.serviceName())
			);
			boolean suppressed = DiagnosticSuppressionFilter.ruleSuppressed(
					DiagnosticRuleId.MISSING_REQUIRED_SERVICE,
					suppressions
			);
			switch (outcome) {
				case UNVERIFIED -> tally.unknown();
				case RUNNING -> tally.pass(WEIGHT_CRITICAL);
				case STOPPED -> tally.failUnlessSuppressed(WEIGHT_CRITICAL, suppressed);
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
			boolean portRuleSuppressed = DiagnosticSuppressionFilter.ruleSuppressed(
					DiagnosticRuleId.PORT_CONFLICT,
					suppressions
			);
			switch (outcome) {
				case UNVERIFIED -> tally.unknown();
				case AVAILABLE -> tally.pass(WEIGHT_SECONDARY);
				case CONFLICT, IN_USE_BY_EXPECTED_SERVICE -> tally.failUnlessSuppressed(
						WEIGHT_SECONDARY,
						portRuleSuppressed
				);
			}
		}

		boolean hasVisibleError = issues.stream().anyMatch(issue -> issue.severity() == DiagnosticSeverity.ERROR);
		Integer percent = percent(tally, hasVisibleError);
		String summary = buildSummary(tally, issues.size(), percent);
		return new ReadinessScore(percent, tally.passedChecks, tally.scoredChecks, tally.unverified, summary);
	}

	private static Integer percent(Tally tally, boolean hasVisibleError) {
		if (tally.scoredWeight == 0) {
			if (hasVisibleError) {
				return 0;
			}
			return null;
		}
		int value = (tally.passedWeight * 100) / tally.scoredWeight;
		if ((hasVisibleError || tally.unverified > 0) && value == 100) {
			return (tally.passedWeight * 100) / (tally.scoredWeight + WEIGHT_CRITICAL);
		}
		return value;
	}

	private static String buildSummary(Tally tally, int issueCount, Integer percent) {
		StringBuilder summary = new StringBuilder();
		if (percent == null) {
			summary.append("Readiness score unavailable");
			if (tally.unverified > 0) {
				summary.append(" · Verified: 0 of ").append(tally.unverified);
			} else {
				summary.append(" · Verified: none");
			}
		} else if (tally.scoredChecks == 0) {
			summary.append("Verified: none");
		} else {
			summary.append("Verified: ").append(tally.passedChecks).append('/').append(tally.scoredChecks)
					.append(" requirements");
		}
		if (tally.unverified > 0 && percent != null) {
			summary.append(" · ").append(tally.unverified).append(" could not verify");
		}
		if (issueCount > 0) {
			summary.append(" · ").append(issueCount).append(issueCount == 1 ? " issue" : " issues");
		}
		return summary.toString();
	}

	private static final class Tally {
		private int passedChecks;
		private int scoredChecks;
		private int passedWeight;
		private int scoredWeight;
		private int unverified;

		private void pass(int weight) {
			passedChecks++;
			scoredChecks++;
			passedWeight += weight;
			scoredWeight += weight;
		}

		private void fail(int weight) {
			failUnlessSuppressed(weight, false);
		}

		private void failUnlessSuppressed(int weight, boolean suppressed) {
			if (suppressed) {
				return;
			}
			scoredChecks++;
			scoredWeight += weight;
		}

		private void unknown() {
			unverified++;
		}
	}
}
