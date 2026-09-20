package com.mbh.initio.detector.make;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.detector.ProjectDetector;
import com.mbh.initio.model.CommandCategory;
import com.mbh.initio.model.CommandOrigin;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.ProjectCommand;
import com.mbh.initio.model.TechnologyCategory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MakefileDetector implements ProjectDetector {

	static final String MAKEFILE = "Makefile";

	private static final Map<String, CommandCategory> TARGET_CATEGORIES = Map.of(
			"run", CommandCategory.RUN,
			"start", CommandCategory.RUN,
			"dev", CommandCategory.DEV,
			"test", CommandCategory.TEST,
			"build", CommandCategory.BUILD,
			"lint", CommandCategory.OTHER
	);

	private final MakefileParser parser;

	public MakefileDetector() {
		this(new MakefileParser());
	}

	MakefileDetector(MakefileParser parser) {
		this.parser = parser;
	}

	@Override
	public boolean supports(ProjectContext context) {
		return context.hasFile(MAKEFILE);
	}

	@Override
	public DetectionResult detect(ProjectContext context) {
		Path makefile = Path.of(MAKEFILE);
		DetectionSource source = new DetectionSource(
				makefile,
				"Makefile targets",
				DetectionConfidence.MEDIUM
		);
		List<ProjectCommand> commands = new ArrayList<>();
		for (String target : parser.parseRecognizedTargets(context.resolve(MAKEFILE))) {
			commands.add(new ProjectCommand(
					target,
					"make " + target,
					TARGET_CATEGORIES.getOrDefault(target, CommandCategory.OTHER),
					CommandOrigin.INFERRED,
					source
			));
		}
		List<DetectedTechnology> technologies = List.of(
				new DetectedTechnology("Make", TechnologyCategory.BUILD_TOOL, source)
		);
		return new DetectionResult(null, technologies, List.of(), List.of(), List.of(), List.of(), commands, List.of());
	}
}
