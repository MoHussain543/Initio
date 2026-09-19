package com.mbh.Initio.model;

import java.util.Objects;

public record ProjectMetadata(String name, String description) {
	public ProjectMetadata {
		Objects.requireNonNull(name, "name");
	}
}
