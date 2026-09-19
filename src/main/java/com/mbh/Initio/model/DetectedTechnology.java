package com.mbh.Initio.model;

import java.util.Objects;

public record DetectedTechnology(
		String name,
		TechnologyCategory category,
		DetectionSource source
) {
	public DetectedTechnology {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(category, "category");
		Objects.requireNonNull(source, "source");
	}
}
