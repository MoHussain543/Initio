package com.mbh.initio.diagnostic.rules;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.diagnostic.DiagnosticRule;
import com.mbh.initio.diagnostic.VersionDriftHelper;
import com.mbh.initio.model.CiExpectation;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.RuntimeRequirement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DeclarationDriftRule implements DiagnosticRule {

	@Override
	public List<DiagnosticIssue> evaluate(AnalysisContext context) {
		List<DiagnosticIssue> issues = new ArrayList<>();
		nodeDeclarationDrift(context).ifPresent(issues::add);
		javaCiDrift(context).ifPresent(issues::add);
		nodeCiDrift(context).ifPresent(issues::add);
		return List.copyOf(issues);
	}

	private static java.util.Optional<DiagnosticIssue> nodeDeclarationDrift(AnalysisContext context) {
		List<RuntimeRequirement> nodeRequirements = context.project().runtimeRequirements().stream()
				.filter(requirement -> "node".equalsIgnoreCase(requirement.runtime()))
				.filter(requirement -> requirement.requiredVersion() != null && !requirement.requiredVersion().isBlank())
				.toList();
		if (nodeRequirements.size() < 2) {
			return java.util.Optional.empty();
		}
		Set<Integer> majors = new LinkedHashSet<>();
		for (RuntimeRequirement requirement : nodeRequirements) {
			VersionDriftHelper.referenceMajor(requirement.requiredVersion()).ifPresent(majors::add);
		}
		if (majors.size() <= 1) {
			return java.util.Optional.empty();
		}
		String detail = nodeRequirements.stream()
				.map(DeclarationDriftRule::formatRuntimeDeclaration)
				.collect(Collectors.joining(System.lineSeparator()));
		return java.util.Optional.of(new DiagnosticIssue(
				DiagnosticSeverity.WARNING,
				"Conflicting Node engine declarations",
				detail
		));
	}

	private static java.util.Optional<DiagnosticIssue> javaCiDrift(AnalysisContext context) {
		List<RuntimeRequirement> javaRequirements = context.project().runtimeRequirements().stream()
				.filter(requirement -> "java".equalsIgnoreCase(requirement.runtime()))
				.filter(requirement -> requirement.requiredVersion() != null && !requirement.requiredVersion().isBlank())
				.toList();
		if (javaRequirements.isEmpty()) {
			return java.util.Optional.empty();
		}
		String declared = javaRequirements.getFirst().requiredVersion();
		List<String> mismatches = new ArrayList<>();
		for (CiExpectation expectation : context.project().ciExpectations()) {
			for (String ciJava : expectation.javaVersions()) {
				if (!VersionDriftHelper.sameMajor(declared, ciJava)) {
					mismatches.add(formatCiDeclaration(expectation, "Java " + ciJava));
				}
			}
		}
		if (mismatches.isEmpty()) {
			return java.util.Optional.empty();
		}
		String detail = formatRuntimeDeclaration(javaRequirements.getFirst()) + System.lineSeparator()
				+ String.join(System.lineSeparator(), mismatches);
		return java.util.Optional.of(new DiagnosticIssue(
				DiagnosticSeverity.WARNING,
				"Conflicting Java versions",
				detail
		));
	}

	private static java.util.Optional<DiagnosticIssue> nodeCiDrift(AnalysisContext context) {
		List<RuntimeRequirement> nodeRequirements = context.project().runtimeRequirements().stream()
				.filter(requirement -> "node".equalsIgnoreCase(requirement.runtime()))
				.filter(requirement -> requirement.requiredVersion() != null && !requirement.requiredVersion().isBlank())
				.toList();
		if (nodeRequirements.isEmpty() || context.project().ciExpectations().isEmpty()) {
			return java.util.Optional.empty();
		}
		List<String> mismatches = new ArrayList<>();
		for (RuntimeRequirement requirement : nodeRequirements) {
			for (CiExpectation expectation : context.project().ciExpectations()) {
				for (String ciNode : expectation.nodeVersions()) {
					if (!VersionDriftHelper.sameMajor(requirement.requiredVersion(), ciNode)) {
						mismatches.add(formatRuntimeDeclaration(requirement) + System.lineSeparator()
								+ formatCiDeclaration(expectation, "Node " + ciNode));
					}
				}
			}
		}
		if (mismatches.isEmpty()) {
			return java.util.Optional.empty();
		}
		return java.util.Optional.of(new DiagnosticIssue(
				DiagnosticSeverity.WARNING,
				"Conflicting Node versions between repo and CI",
				String.join(System.lineSeparator() + System.lineSeparator(), mismatches)
		));
	}

	private static String formatRuntimeDeclaration(RuntimeRequirement requirement) {
		String runtime = capitalize(requirement.runtime());
		String version = requirement.requiredVersion();
		return runtime + (version == null || version.isBlank() ? "" : " " + version)
				+ System.lineSeparator()
				+ "Source: " + requirement.source().file();
	}

	private static String formatCiDeclaration(CiExpectation expectation, String label) {
		return label + System.lineSeparator() + "Source: " + expectation.workflowFile();
	}

	private static String capitalize(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}

}
