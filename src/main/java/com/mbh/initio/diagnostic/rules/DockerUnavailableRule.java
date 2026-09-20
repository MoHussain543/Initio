package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.diagnostic.DiagnosticRule;
import com.mbh.initio.diagnostic.DiagnosticRuleId;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.RequirementStatus;
import com.mbh.initio.model.VerificationState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DockerUnavailableRule implements DiagnosticRule {

	@Override
	public List<DiagnosticIssue> evaluate(AnalysisContext context) {
		if (context.project().serviceRequirements().isEmpty()) {
			return List.of();
		}
		Optional<InstalledRuntime> docker = context.local().installedRuntime("docker");
		if (docker.isEmpty()) {
			return List.of();
		}
		InstalledRuntime installed = docker.get();
		if (installed.verificationState() == VerificationState.UNVERIFIED) {
			return List.of();
		}
		if (installed.status() != RequirementStatus.MISSING) {
			return List.of();
		}
		List<DiagnosticIssue> issues = new ArrayList<>();
		issues.add(new DiagnosticIssue(
				DiagnosticRuleId.DOCKER_UNAVAILABLE,
				DiagnosticSeverity.ERROR,
				"Docker is not available",
				"Install Docker Desktop or the Docker Engine, then run docker compose up for this project"
		));
		return issues;
	}
}
