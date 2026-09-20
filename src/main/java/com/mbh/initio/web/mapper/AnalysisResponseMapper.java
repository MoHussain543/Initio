package com.mbh.initio.web.mapper;

import com.mbh.initio.analysis.AnalysisResult;
import com.mbh.initio.diagnostic.EnvironmentRequirementEvaluator;
import com.mbh.initio.diagnostic.PortExpectationEvaluator;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator;
import com.mbh.initio.diagnostic.RuntimeRequirementEvaluator.Outcome;
import com.mbh.initio.diagnostic.ServiceRequirementEvaluator;
import com.mbh.initio.model.CiExpectation;
import com.mbh.initio.model.CommandCategory;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortObservation;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectCommand;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.ServiceStatus;
import com.mbh.initio.web.dto.AnalysisResponse;
import com.mbh.initio.web.dto.CiResponse;
import com.mbh.initio.web.dto.CommandResponse;
import com.mbh.initio.web.dto.EnvironmentRowResponse;
import com.mbh.initio.web.dto.IssueResponse;
import com.mbh.initio.web.dto.PortRowResponse;
import com.mbh.initio.web.dto.ProjectResponse;
import com.mbh.initio.web.dto.ReadinessResponse;
import com.mbh.initio.web.dto.RuntimeRowResponse;
import com.mbh.initio.web.dto.ServiceRowResponse;
import com.mbh.initio.web.dto.TechnologyResponse;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class AnalysisResponseMapper {

	public AnalysisResponse toResponse(AnalysisResult result) {
		ProjectAnalysis project = result.project();
		return new AnalysisResponse(
				toProject(project),
				toReadiness(result),
				toRuntimeRows(result),
				toEnvironmentRows(result),
				toServiceRows(result),
				toPortRows(result),
				toIssues(result.issues()),
				toCommands(project),
				toCi(project)
		);
	}

	private static ProjectResponse toProject(ProjectAnalysis project) {
		List<TechnologyResponse> technologies = project.technologies().stream()
				.sorted(Comparator
						.comparing((DetectedTechnology technology) -> technology.category().ordinal())
						.thenComparing(DetectedTechnology::name))
				.map(technology -> new TechnologyResponse(technology.name(), technology.category().name()))
				.toList();
		return new ProjectResponse(
				project.metadata().name(),
				project.projectPath().toString(),
				technologies
		);
	}

	private static ReadinessResponse toReadiness(AnalysisResult result) {
		return new ReadinessResponse(
				result.readiness().percent(),
				result.readiness().summary(),
				result.issues().size()
		);
	}

	private static List<RuntimeRowResponse> toRuntimeRows(AnalysisResult result) {
		return result.project().runtimeRequirements().stream()
				.map(requirement -> toRuntimeRow(requirement, result))
				.toList();
	}

	private static RuntimeRowResponse toRuntimeRow(RuntimeRequirement requirement, AnalysisResult result) {
		Optional<InstalledRuntime> installed = result.local().installedRuntime(requirement.runtime());
		Outcome outcome = RuntimeRequirementEvaluator.outcome(requirement, installed);
		String label = formatDeclaredRequirement(requirement);
		return new RuntimeRowResponse(
				requirement.runtime(),
				requirement.requiredVersion(),
				installed.map(InstalledRuntime::detectedVersion).orElse(null),
				outcome.name(),
				label
		);
	}

	private static List<EnvironmentRowResponse> toEnvironmentRows(AnalysisResult result) {
		return result.project().environmentVariableRequirements().stream()
				.map(requirement -> toEnvironmentRow(requirement, result))
				.toList();
	}

	private static EnvironmentRowResponse toEnvironmentRow(
			EnvironmentVariableRequirement requirement,
			AnalysisResult result
	) {
		EnvironmentRequirementEvaluator.Outcome outcome = EnvironmentRequirementEvaluator.outcome(
				result.local().environmentVariableStatus(requirement.name())
		);
		return new EnvironmentRowResponse(requirement.name(), outcome.name());
	}

	private static List<ServiceRowResponse> toServiceRows(AnalysisResult result) {
		return result.project().serviceRequirements().stream()
				.map(requirement -> toServiceRow(requirement, result))
				.toList();
	}

	private static ServiceRowResponse toServiceRow(ServiceRequirement requirement, AnalysisResult result) {
		ServiceRequirementEvaluator.Outcome outcome = ServiceRequirementEvaluator.outcome(
				result.local().serviceStatus(requirement.serviceName())
		);
		return new ServiceRowResponse(
				requirement.serviceName(),
				blankToNull(requirement.image()),
				outcome.name()
		);
	}

	private static List<PortRowResponse> toPortRows(AnalysisResult result) {
		return result.project().portExpectations().stream()
				.map(expectation -> toPortRow(expectation, result))
				.toList();
	}

	private static PortRowResponse toPortRow(PortExpectation expectation, AnalysisResult result) {
		PortExpectationEvaluator.Outcome outcome = PortExpectationEvaluator.outcome(
				expectation,
				result.local().portObservation(expectation.port()),
				linkedService(result, expectation.port())
		);
		Optional<PortObservation> observation = result.local().portObservation(expectation.port());
		return new PortRowResponse(
				expectation.port(),
				expectation.label(),
				outcome.name(),
				observation.map(PortObservation::occupantHint).orElse(null)
		);
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

	private static List<IssueResponse> toIssues(List<DiagnosticIssue> issues) {
		return issues.stream()
				.sorted(Comparator
						.comparing((DiagnosticIssue issue) -> severityRank(issue.severity()))
						.thenComparing(DiagnosticIssue::title))
				.map(AnalysisResponseMapper::toIssue)
				.toList();
	}

	private static IssueResponse toIssue(DiagnosticIssue issue) {
		return new IssueResponse(
				issue.severity().name(),
				issue.title(),
				issue.detail(),
				extractCopyText(issue.detail())
		);
	}

	private static List<CommandResponse> toCommands(ProjectAnalysis project) {
		return project.projectCommands().stream()
				.sorted(Comparator
						.comparing((ProjectCommand command) -> categoryRank(command.category()))
						.thenComparing(ProjectCommand::name))
				.map(AnalysisResponseMapper::toCommand)
				.toList();
	}

	private static CommandResponse toCommand(ProjectCommand command) {
		Path sourceFile = command.source().file();
		return new CommandResponse(
				command.name(),
				command.command(),
				command.category().name(),
				command.origin().name(),
				sourceFile.toString(),
				command.source().description()
		);
	}

	private static List<CiResponse> toCi(ProjectAnalysis project) {
		return project.ciExpectations().stream()
				.map(AnalysisResponseMapper::toCiEntry)
				.toList();
	}

	private static CiResponse toCiEntry(CiExpectation expectation) {
		return new CiResponse(
				expectation.workflowFile().toString(),
				expectation.javaVersions(),
				expectation.nodeVersions(),
				expectation.runCommands()
		);
	}

	private static int severityRank(DiagnosticSeverity severity) {
		return switch (severity) {
			case ERROR -> 0;
			case WARNING -> 1;
			case INFO -> 2;
		};
	}

	private static int categoryRank(CommandCategory category) {
		return switch (category) {
			case RUN -> 0;
			case DEV -> 1;
			case TEST -> 2;
			case BUILD -> 3;
			case OTHER -> 4;
		};
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

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	private static String extractCopyText(String detail) {
		if (detail == null || detail.isBlank()) {
			return null;
		}
		for (String line : detail.lines().toList()) {
			String trimmed = line.strip();
			if (looksLikeCopyableCommand(trimmed)) {
				return trimmed;
			}
		}
		return null;
	}

	private static boolean looksLikeCopyableCommand(String line) {
		return line.startsWith("docker ")
				|| line.startsWith("./mvnw")
				|| line.startsWith("mvn ")
				|| line.startsWith("npm ")
				|| line.startsWith("make ");
	}
}
