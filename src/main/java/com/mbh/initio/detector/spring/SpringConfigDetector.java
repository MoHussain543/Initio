package com.mbh.initio.detector.spring;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.detector.ProjectDetector;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortRole;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SpringConfigDetector implements ProjectDetector {

	private final SpringApplicationPropertiesParser propertiesParser;
	private final SpringApplicationYamlParser yamlParser;

	public SpringConfigDetector() {
		this(new SpringApplicationPropertiesParser(), new SpringApplicationYamlParser());
	}

	SpringConfigDetector(
			SpringApplicationPropertiesParser propertiesParser,
			SpringApplicationYamlParser yamlParser
	) {
		this.propertiesParser = propertiesParser;
		this.yamlParser = yamlParser;
	}

	@Override
	public boolean supports(ProjectContext context) {
		return SpringConfigPaths.anyPresent(context);
	}

	@Override
	public DetectionResult detect(ProjectContext context) {
		for (Path configFile : SpringConfigPaths.CONFIG_FILES) {
			if (!context.hasFile(configFile.toString())) {
				continue;
			}
			Optional<Integer> port = parsePort(context, configFile);
			if (port.isEmpty()) {
				continue;
			}
			DetectionSource source = new DetectionSource(
					configFile,
					"Application HTTP port from server.port",
					DetectionConfidence.HIGH
			);
			List<PortExpectation> portExpectations = new ArrayList<>();
			portExpectations.add(new PortExpectation(port.get(), PortRole.APPLICATION, "Spring Boot application", source));
			return new DetectionResult(null, List.of(), List.of(), List.of(), List.of(), portExpectations, List.of(), List.of());
		}
		return DetectionResult.empty();
	}

	private Optional<Integer> parsePort(ProjectContext context, Path configFile) {
		Path absolute = context.resolve(configFile.toString());
		String filename = configFile.getFileName().toString().toLowerCase();
		if (filename.endsWith(".properties")) {
			return propertiesParser.parseServerPort(absolute);
		}
		return yamlParser.parseServerPort(absolute);
	}
}
