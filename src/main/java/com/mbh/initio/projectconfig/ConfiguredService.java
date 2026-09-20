package com.mbh.initio.projectconfig;

import java.util.Objects;

public record ConfiguredService(String name, int port) {
	public ConfiguredService {
		Objects.requireNonNull(name, "name");
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("port must be between 1 and 65535");
		}
	}
}
