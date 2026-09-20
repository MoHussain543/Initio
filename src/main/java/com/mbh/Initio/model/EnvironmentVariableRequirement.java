package com.mbh.initio.model;

import java.util.Objects;

public record EnvironmentVariableRequirement(String name, DetectionSource source) {
	public EnvironmentVariableRequirement {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(source, "source");
	}
}
