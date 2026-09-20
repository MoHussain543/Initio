package com.mbh.initio.detector.docker;

import com.mbh.initio.analysis.ProjectContext;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class ComposeFileLocator {

	static final List<String> COMPOSE_FILENAMES = List.of(
			"compose.yaml",
			"compose.yml",
			"docker-compose.yaml",
			"docker-compose.yml"
	);

	public Optional<Path> locate(ProjectContext context) {
		for (String filename : COMPOSE_FILENAMES) {
			if (context.hasFile(filename)) {
				return Optional.of(Path.of(filename));
			}
		}
		return Optional.empty();
	}
}
