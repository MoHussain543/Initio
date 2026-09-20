package com.mbh.initio.model;

import java.util.Objects;

public record PortExpectation(
		int port,
		PortRole role,
		String label,
		DetectionSource source
) {
	public PortExpectation {
		Objects.requireNonNull(role, "role");
		Objects.requireNonNull(label, "label");
		Objects.requireNonNull(source, "source");
		if (port <= 0 || port > 65535) {
			throw new IllegalArgumentException("Invalid port: " + port);
		}
	}
}
