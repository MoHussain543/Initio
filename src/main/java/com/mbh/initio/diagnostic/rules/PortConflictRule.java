package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.diagnostic.DiagnosticRule;
import com.mbh.initio.diagnostic.DiagnosticRuleId;
import com.mbh.initio.diagnostic.PortExpectationEvaluator;
import com.mbh.initio.diagnostic.PortExpectationEvaluator.Outcome;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortObservation;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.ServiceStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PortConflictRule implements DiagnosticRule {

	@Override
	public List<DiagnosticIssue> evaluate(AnalysisContext context) {
		List<DiagnosticIssue> issues = new ArrayList<>();
		for (PortExpectation expectation : context.project().portExpectations()) {
			Outcome outcome = PortExpectationEvaluator.outcome(
					expectation,
					context.local().portObservation(expectation.port()),
					linkedService(context, expectation.port())
			);
			if (outcome != Outcome.CONFLICT) {
				continue;
			}
			Optional<PortObservation> observation = context.local().portObservation(expectation.port());
			String occupant = observation.map(PortObservation::occupantHint).orElse("unknown process");
			DiagnosticSeverity severity = expectation.role() == PortRole.APPLICATION
					? DiagnosticSeverity.ERROR
					: DiagnosticSeverity.WARNING;
			String title = "Port " + expectation.port() + " is occupied";
			String detail = expectation.role() == PortRole.APPLICATION
					? "Free port " + expectation.port() + " for the application (currently used by " + occupant + ")"
					: "Port " + expectation.port() + " is in use by " + occupant
							+ " but expected service " + expectation.label() + " is not running";
			issues.add(DiagnosticIssue.forPort(
					DiagnosticRuleId.PORT_CONFLICT,
					severity,
					title,
					detail,
					expectation.port()
			));
		}
		return issues;
	}

	private static Optional<ServiceStatus> linkedService(AnalysisContext context, int port) {
		Optional<ServiceRequirement> requirement = context.project().serviceRequirements().stream()
				.filter(entry -> entry.publishedHostPorts().contains(port))
				.findFirst();
		if (requirement.isEmpty()) {
			return Optional.empty();
		}
		return context.local().serviceStatus(requirement.get().serviceName());
	}
}
