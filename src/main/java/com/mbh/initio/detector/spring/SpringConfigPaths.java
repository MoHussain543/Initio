package com.mbh.initio.detector.spring;

import com.mbh.initio.analysis.ProjectContext;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

final class SpringConfigPaths {

	static final List<Path> CONFIG_FILES = List.of(
			Path.of("src/main/resources/application.properties"),
			Path.of("src/main/resources/application.yml"),
			Path.of("src/main/resources/application.yaml")
	);

	private SpringConfigPaths() {
	}

	static Optional<Path> firstPresent(ProjectContext context) {
		for (Path configFile : CONFIG_FILES) {
			if (context.hasFile(configFile.toString())) {
				return Optional.of(configFile);
			}
		}
		return Optional.empty();
	}

	static boolean anyPresent(ProjectContext context) {
		return firstPresent(context).isPresent();
	}
}
