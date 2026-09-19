package com.mbh.Initio.model;

import java.nio.file.Path;
import java.util.Objects;

public record DetectionSource(
		Path file,
		String description,
		DetectionConfidence confidence
) {
	public DetectionSource {
		Objects.requireNonNull(file, "file");
		Objects.requireNonNull(description, "description");
		Objects.requireNonNull(confidence, "confidence");
	}
}
