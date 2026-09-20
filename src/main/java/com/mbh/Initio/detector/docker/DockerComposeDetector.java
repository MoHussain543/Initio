package com.mbh.initio.detector.docker;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.detector.ProjectDetector;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.model.ServiceRequirement;
import com.mbh.initio.model.TechnologyCategory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public final class DockerComposeDetector implements ProjectDetector {

	private final ComposeFileLocator composeFileLocator;
	private final DockerComposeParser parser;

	public DockerComposeDetector() {
		this(new ComposeFileLocator(), new DockerComposeParser());
	}

	DockerComposeDetector(ComposeFileLocator composeFileLocator, DockerComposeParser parser) {
		this.composeFileLocator = composeFileLocator;
		this.parser = parser;
	}

	@Override
	public boolean supports(ProjectContext context) {
		return composeFileLocator.locate(context).isPresent();
	}

	@Override
	public DetectionResult detect(ProjectContext context) {
		Path composeFile = composeFileLocator.locate(context).orElseThrow();
		Path composePath = context.resolve(composeFile.toString());
		DetectionSource source = new DetectionSource(
				composeFile,
				"Service from Docker Compose",
				DetectionConfidence.HIGH
		);
		List<DetectedTechnology> technologies = List.of(
				new DetectedTechnology("Docker Compose", TechnologyCategory.TOOL, source)
		);
		List<ServiceRequirement> serviceRequirements = new ArrayList<>();
		List<PortExpectation> portExpectations = new ArrayList<>();
		Map<Integer, PortExpectation> portsByNumber = new LinkedHashMap<>();

		for (ComposeServiceDefinition service : parser.parseServices(composePath)) {
			serviceRequirements.add(new ServiceRequirement(
					service.name(),
					composeFile,
					service.image(),
					service.publishedHostPorts(),
					source
			));
			for (Integer port : service.publishedHostPorts()) {
				String label = service.name();
				if (service.image() != null && !service.image().isBlank()) {
					label = service.name() + " (" + service.image() + ")";
				}
				portsByNumber.putIfAbsent(
						port,
						new PortExpectation(port, PortRole.EXPECTED_SERVICE, label, source)
				);
			}
		}
		portExpectations.addAll(portsByNumber.values());

		return new DetectionResult(null, technologies, List.of(), List.of(), serviceRequirements, portExpectations);
	}
}
