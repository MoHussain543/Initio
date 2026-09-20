package com.mbh.initio.detector.spring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class SpringApplicationPropertiesParser {

	public Optional<Integer> parseServerPort(Path propertiesFile) {
		try {
			for (String line : Files.readAllLines(propertiesFile)) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}
				if (trimmed.startsWith("server.port=")) {
					return parsePort(trimmed.substring("server.port=".length()));
				}
			}
			return Optional.empty();
		} catch (IOException exception) {
			return Optional.empty();
		}
	}

	private static Optional<Integer> parsePort(String value) {
		String trimmed = value.trim();
		if (trimmed.isEmpty() || !trimmed.chars().allMatch(Character::isDigit)) {
			return Optional.empty();
		}
		int port = Integer.parseInt(trimmed);
		if (port <= 0 || port > 65535) {
			return Optional.empty();
		}
		return Optional.of(port);
	}
}
