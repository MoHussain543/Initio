package com.mbh.Initio.cli;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectPathResolver {

	private ProjectPathResolver() {
	}

	public static Path resolve(Path path) {
		Path candidate = path == null ? Path.of(".") : path;
		Path normalized = candidate.toAbsolutePath().normalize();
		if (!Files.exists(normalized)) {
			throw new IllegalArgumentException("Project path does not exist: " + normalized);
		}
		if (!Files.isDirectory(normalized)) {
			throw new IllegalArgumentException("Project path is not a directory: " + normalized);
		}
		return normalized;
	}
}
