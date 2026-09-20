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

	static final String APPLICATION_PROPERTIES = "src/main/resources/application.properties";

	private final SpringApplicationPropertiesParser propertiesParser;

	public SpringConfigDetector() {
		this(new SpringApplicationPropertiesParser());
	}

	SpringConfigDetector(SpringApplicationPropertiesParser propertiesParser) {
		this.propertiesParser = propertiesParser;
	}

	@Override
	public boolean supports(ProjectContext context) {
		return context.hasFile(APPLICATION_PROPERTIES);
	}

	@Override
	public DetectionResult detect(ProjectContext context) {
		Path propertiesPath = Path.of(APPLICATION_PROPERTIES);
		Optional<Integer> port = propertiesParser.parseServerPort(context.resolve(APPLICATION_PROPERTIES));
		if (port.isEmpty()) {
			return DetectionResult.empty();
		}
		DetectionSource source = new DetectionSource(
				propertiesPath,
				"Application HTTP port from server.port",
				DetectionConfidence.HIGH
		);
		List<PortExpectation> portExpectations = new ArrayList<>();
		portExpectations.add(new PortExpectation(port.get(), PortRole.APPLICATION, "Spring Boot application", source));
		return new DetectionResult(null, List.of(), List.of(), List.of(), List.of(), portExpectations, List.of());
	}
}
