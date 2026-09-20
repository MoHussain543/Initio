package com.mbh.initio.system;

import com.mbh.initio.model.PresenceStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class DotEnvPresenceScanner {

	enum KeyState {
		NOT_FOUND,
		EMPTY,
		HAS_VALUE
	}

	private DotEnvPresenceScanner() {
	}

	static KeyState keyState(Path dotEnvFile, String variableName) throws IOException {
		if (!Files.isRegularFile(dotEnvFile)) {
			return KeyState.NOT_FOUND;
		}
		try (var lines = Files.lines(dotEnvFile)) {
			return lines.map(DotEnvPresenceScanner::parseLine)
					.filter(parsed -> parsed != null && parsed.key.equals(variableName))
					.findFirst()
					.map(parsed -> parsed.hasValue ? KeyState.HAS_VALUE : KeyState.EMPTY)
					.orElse(KeyState.NOT_FOUND);
		}
	}

	private static ParsedLine parseLine(String line) {
		if (line == null) {
			return null;
		}
		String trimmed = line.trim();
		if (trimmed.isEmpty() || trimmed.startsWith("#")) {
			return null;
		}
		if (trimmed.startsWith("export ")) {
			trimmed = trimmed.substring("export ".length()).trim();
		}
		int separator = trimmed.indexOf('=');
		if (separator <= 0) {
			return null;
		}
		String key = trimmed.substring(0, separator).trim();
		if (key.isEmpty()) {
			return null;
		}
		boolean hasValue = !trimmed.substring(separator + 1).trim().isEmpty();
		return new ParsedLine(key, hasValue);
	}

	private record ParsedLine(String key, boolean hasValue) {
	}
}
