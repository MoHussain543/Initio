package com.mbh.initio.model;

import java.util.Objects;

public record ProjectCommand(
		String name,
		String command,
		CommandCategory category,
		CommandOrigin origin,
		DetectionSource source
) {
	public ProjectCommand {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(command, "command");
		Objects.requireNonNull(category, "category");
		Objects.requireNonNull(origin, "origin");
		Objects.requireNonNull(source, "source");
	}
}
