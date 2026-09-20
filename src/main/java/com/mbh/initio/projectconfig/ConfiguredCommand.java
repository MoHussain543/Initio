package com.mbh.initio.projectconfig;

import com.mbh.initio.model.CommandCategory;

import java.util.Objects;

public record ConfiguredCommand(
		String name,
		CommandCategory category,
		String command
) {
	public ConfiguredCommand {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(category, "category");
		Objects.requireNonNull(command, "command");
	}
}
