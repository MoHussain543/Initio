package com.mbh.Initio.analysis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public record ProjectContext(Path root) {
	public ProjectContext {
		Objects.requireNonNull(root, "root");
		root = root.toAbsolutePath().normalize();
	}

	public boolean hasFile(String relativePath) {
		Objects.requireNonNull(relativePath, "relativePath");
		return Files.isRegularFile(root.resolve(relativePath));
	}

	public Path resolve(String relativePath) {
		Objects.requireNonNull(relativePath, "relativePath");
		return root.resolve(relativePath);
	}
}
