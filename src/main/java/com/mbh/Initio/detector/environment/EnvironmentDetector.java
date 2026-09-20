package com.mbh.initio.detector.environment;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.detector.ProjectDetector;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.EnvironmentVariableRequirement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class EnvironmentDetector implements ProjectDetector {

	static final String ENV_EXAMPLE = ".env.example";

	private final EnvFileParser parser;

	public EnvironmentDetector() {
		this(new EnvFileParser());
	}

	EnvironmentDetector(EnvFileParser parser) {
		this.parser = parser;
	}

	@Override
	public boolean supports(ProjectContext context) {
		return context.hasFile(ENV_EXAMPLE);
	}

	@Override
	public DetectionResult detect(ProjectContext context) {
		Path envExample = context.resolve(ENV_EXAMPLE);
		DetectionSource source = new DetectionSource(
				Path.of(ENV_EXAMPLE),
				"Expected variables from .env.example",
				DetectionConfidence.HIGH
		);
		List<EnvironmentVariableRequirement> requirements = new ArrayList<>();
		for (String key : parser.parseKeys(envExample)) {
			requirements.add(new EnvironmentVariableRequirement(key, source));
		}
		return new DetectionResult(null, List.of(), List.of(), requirements);
	}
}
