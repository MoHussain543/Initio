package com.mbh.initio.detector.github;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.detector.ProjectDetector;
import com.mbh.initio.model.CiExpectation;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.TechnologyCategory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GitHubActionsDetector implements ProjectDetector {

	private final GitHubWorkflowLocator workflowLocator;
	private final GitHubActionsWorkflowParser workflowParser;

	public GitHubActionsDetector() {
		this(new GitHubWorkflowLocator(), new GitHubActionsWorkflowParser());
	}

	GitHubActionsDetector(GitHubWorkflowLocator workflowLocator, GitHubActionsWorkflowParser workflowParser) {
		this.workflowLocator = workflowLocator;
		this.workflowParser = workflowParser;
	}

	@Override
	public boolean supports(ProjectContext context) {
		return !workflowLocator.locate(context).isEmpty();
	}

	@Override
	public DetectionResult detect(ProjectContext context) {
		List<Path> workflowFiles = workflowLocator.locate(context);
		List<CiExpectation> ciExpectations = new ArrayList<>();
		Map<String, DetectedTechnology> technologies = new LinkedHashMap<>();

		for (Path workflowFile : workflowFiles) {
			Path absoluteWorkflow = context.resolve(workflowFile.toString());
			GitHubActionsWorkflowParser.ParsedWorkflow parsed = workflowParser.parse(absoluteWorkflow);
			DetectionSource source = new DetectionSource(
					workflowFile,
					"GitHub Actions workflow",
					DetectionConfidence.HIGH
			);
			ciExpectations.add(new CiExpectation(
					workflowFile,
					parsed.javaVersions(),
					parsed.nodeVersions(),
					parsed.runCommands(),
					source
			));
			technologies.putIfAbsent(
					"GitHub Actions",
					new DetectedTechnology("GitHub Actions", TechnologyCategory.TOOL, source)
			);
		}

		return new DetectionResult(
				null,
				List.copyOf(technologies.values()),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				List.of(),
				ciExpectations
		);
	}
}
