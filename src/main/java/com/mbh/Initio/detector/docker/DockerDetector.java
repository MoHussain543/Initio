package com.mbh.initio.detector.docker;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.detector.ProjectDetector;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.TechnologyCategory;

import java.nio.file.Path;
import java.util.List;

public final class DockerDetector implements ProjectDetector {

	static final String DOCKERFILE = "Dockerfile";

	@Override
	public boolean supports(ProjectContext context) {
		return context.hasFile(DOCKERFILE);
	}

	@Override
	public DetectionResult detect(ProjectContext context) {
		DetectionSource source = new DetectionSource(
				Path.of(DOCKERFILE),
				"Container image build definition",
				DetectionConfidence.HIGH
		);
		DetectedTechnology docker = new DetectedTechnology("Docker", TechnologyCategory.TOOL, source);
		return new DetectionResult(null, List.of(docker), List.of(), List.of(), List.of(), List.of(), List.of());
	}
}
