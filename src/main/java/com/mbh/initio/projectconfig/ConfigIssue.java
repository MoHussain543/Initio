package com.mbh.initio.projectconfig;

import java.util.Objects;

public record ConfigIssue(String path, String message) {
	public ConfigIssue {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(message, "message");
	}
}
