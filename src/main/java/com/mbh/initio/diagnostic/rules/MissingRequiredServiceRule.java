package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.diagnostic.DiagnosticRule;
import com.mbh.initio.diagnostic.DiagnosticRuleId;
import com.mbh.initio.diagnostic.ServiceRequirementEvaluator;
import com.mbh.initio.diagnostic.ServiceRequirementEvaluator.Outcome;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.RequirementStatus;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.VerificationState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MissingRequiredServiceRule implements DiagnosticRule {

	@Override
	public List<DiagnosticIssue> evaluate(AnalysisContext context) {
		List<DiagnosticIssue> issues = new ArrayList<>();
		boolean dockerUsable = dockerUsable(context);
		for (ServiceRequirement requirement : context.project().serviceRequirements()) {
			if (requirement.composeBacked()) {
				if (!dockerUsable) {
					continue;
				}
				Outcome outcome = ServiceRequirementEvaluator.outcome(
						context.local().serviceStatus(requirement.serviceName())
				);
				if (outcome != Outcome.STOPPED) {
					continue;
				}
				String detail = "Run docker compose up -d " + requirement.serviceName()
						+ " (from " + requirement.composeFile() + ")";
				issues.add(new DiagnosticIssue(
						DiagnosticRuleId.MISSING_REQUIRED_SERVICE,
						DiagnosticSeverity.ERROR,
						requirement.serviceName() + " is not running",
						detail,
						requirement.composeFile()
				));
				continue;
			}
			Outcome outcome = ServiceRequirementEvaluator.outcome(
					context.local().serviceStatus(requirement.serviceName())
			);
			if (outcome != Outcome.STOPPED) {
				continue;
			}
			int port = requirement.publishedHostPorts().isEmpty()
					? -1
					: requirement.publishedHostPorts().getFirst();
			String portLabel = port > 0 ? String.valueOf(port) : "its configured port";
			issues.add(new DiagnosticIssue(
					DiagnosticRuleId.MISSING_REQUIRED_SERVICE,
					DiagnosticSeverity.ERROR,
					"Configured service \"" + requirement.serviceName() + "\" was not detected",
					"Configured service \"" + requirement.serviceName() + "\" could not be detected on port "
							+ portLabel + ".",
					requirement.source().file()
			));
		}
		return issues;
	}

	private static boolean dockerUsable(AnalysisContext context) {
		if (context.project().serviceRequirements().stream().noneMatch(ServiceRequirement::composeBacked)) {
			return false;
		}
		Optional<InstalledRuntime> docker = context.local().installedRuntime("docker");
		return docker.isPresent()
				&& docker.get().verificationState() != VerificationState.UNVERIFIED
				&& docker.get().status() != RequirementStatus.MISSING;
	}
}
