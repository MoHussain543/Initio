package com.mbh.initio.cli.format;

import com.mbh.initio.analysis.AnalysisResult;
import com.mbh.initio.diagnostic.EnvironmentRequirementEvaluator;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator.Outcome;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.diagnostic.PortExpectationEvaluator;
import com.mbh.initio.diagnostic.ServiceRequirementEvaluator;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortObservation;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.ServiceStatus;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class CheckReportFormatter {

	public void write(AnalysisResult result, PrintWriter out) {
		ProjectAnalysis project = result.project();
		out.println("INITIO");
		ReportLayout.blank(out);
		out.println("Project: " + project.metadata().name());
		out.println("Path: " + project.projectPath());
		ReportLayout.blank(out);

		ReportLayout.section(out, "Detected stack");
		for (DetectedTechnology technology : ordered(project.technologies())) {
			out.println(technology.name());
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Declared requirements");
		for (RuntimeRequirement requirement : project.runtimeRequirements()) {
			out.println(formatDeclaredRequirement(requirement));
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Runtime");
		if (project.runtimeRequirements().isEmpty()) {
			out.println("No runtime requirements declared");
		} else {
			for (RuntimeRequirement requirement : project.runtimeRequirements()) {
				out.println(formatRuntimeCheck(requirement, result));
			}
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Services");
		if (project.serviceRequirements().isEmpty()) {
			out.println("No compose services declared");
		} else {
			for (ServiceRequirement requirement : project.serviceRequirements()) {
				out.println(formatServiceCheck(requirement, result));
			}
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Environment");
		if (project.environmentVariableRequirements().isEmpty()) {
			out.println("No environment variables declared");
		} else {
			for (EnvironmentVariableRequirement requirement : project.environmentVariableRequirements()) {
				out.println(formatEnvironmentCheck(requirement, result));
			}
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Ports");
		if (project.portExpectations().isEmpty()) {
			out.println("No ports declared");
		} else {
			for (PortExpectation expectation : project.portExpectations()) {
				out.println(formatPortCheck(expectation, result));
			}
		}
		ReportLayout.blank(out);

		ReportLayout.section(out, "Result");
		out.println("Project readiness: " + result.readiness().percent() + "%");
		out.println(result.readiness().summary());
	}

	private static String formatRuntimeCheck(RuntimeRequirement requirement, AnalysisResult result) {
		Optional<InstalledRuntime> installed = result.local().installedRuntime(requirement.runtime());
		Outcome outcome = RuntimeRequirementEvaluator.outcome(requirement, installed);
		String label = formatDeclaredRequirement(requirement);
		return switch (outcome) {
			case UNVERIFIED -> label + " — Could not verify";
			case MISSING -> "✗ " + label + " — not installed";
			case INCOMPATIBLE -> "✗ " + label + " — incompatible (installed "
					+ installed.map(InstalledRuntime::detectedVersion).orElse("unknown") + ")";
			case SATISFIED -> "✓ " + label + " — installed ("
					+ installed.map(InstalledRuntime::detectedVersion).orElse("unknown") + ")";
		};
	}

	private static String formatServiceCheck(ServiceRequirement requirement, AnalysisResult result) {
		ServiceRequirementEvaluator.Outcome outcome = ServiceRequirementEvaluator.outcome(
				result.local().serviceStatus(requirement.serviceName())
		);
		String label = requirement.serviceName();
		if (requirement.image() != null && !requirement.image().isBlank()) {
			label = label + " (" + requirement.image() + ")";
		}
		return switch (outcome) {
			case UNVERIFIED -> label + " — Could not verify";
			case STOPPED -> "✗ " + label + " — not running";
			case RUNNING -> "✓ " + label + " — running";
		};
	}

	private static String formatPortCheck(PortExpectation expectation, AnalysisResult result) {
		PortExpectationEvaluator.Outcome outcome = PortExpectationEvaluator.outcome(
				expectation,
				result.local().portObservation(expectation.port()),
				linkedService(result, expectation.port())
		);
		String label = expectation.port() + " — " + expectation.label();
		Optional<PortObservation> observation = result.local().portObservation(expectation.port());
		String occupant = observation.map(PortObservation::occupantHint).orElse(null);
		return switch (outcome) {
			case UNVERIFIED -> label + " — Could not verify";
			case AVAILABLE -> "✓ " + label + " — available";
			case NOT_IN_USE -> label + " — not in use";
			case IN_USE_BY_EXPECTED_SERVICE -> {
				if (occupant != null && !occupant.isBlank()) {
					yield "✓ " + label + " — in use by " + occupant;
				}
				yield "✓ " + label + " — in use by expected service";
			}
			case CONFLICT -> {
				if (expectation.role() == PortRole.APPLICATION) {
					yield "✗ " + label + " — occupied"
							+ (occupant == null ? "" : " (" + occupant + ")");
				}
				yield "✗ " + label + " — unexpected occupant"
						+ (occupant == null ? "" : " (" + occupant + ")");
			}
		};
	}

	private static Optional<ServiceStatus> linkedService(AnalysisResult result, int port) {
		Optional<ServiceRequirement> requirement = result.project().serviceRequirements().stream()
				.filter(entry -> entry.publishedHostPorts().contains(port))
				.findFirst();
		if (requirement.isEmpty()) {
			return Optional.empty();
		}
		return result.local().serviceStatus(requirement.get().serviceName());
	}

	private static String formatEnvironmentCheck(EnvironmentVariableRequirement requirement, AnalysisResult result) {
		EnvironmentRequirementEvaluator.Outcome outcome = EnvironmentRequirementEvaluator.outcome(
				result.local().environmentVariableStatus(requirement.name())
		);
		return switch (outcome) {
			case UNVERIFIED -> requirement.name() + " — Could not verify";
			case MISSING -> "✗ " + requirement.name() + " — missing";
			case EMPTY -> "✗ " + requirement.name() + " — empty";
			case SATISFIED -> "✓ " + requirement.name() + " — present";
		};
	}

	private static List<DetectedTechnology> ordered(List<DetectedTechnology> technologies) {
		return technologies.stream()
				.sorted(Comparator
						.comparing((DetectedTechnology technology) -> technology.category().ordinal())
						.thenComparing(DetectedTechnology::name))
				.toList();
	}

	private static String formatDeclaredRequirement(RuntimeRequirement requirement) {
		String label = capitalize(requirement.runtime());
		if (requirement.requiredVersion() == null || requirement.requiredVersion().isBlank()) {
			return label;
		}
		return label + " " + requirement.requiredVersion();
	}

	private static String capitalize(String value) {
		if (value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
