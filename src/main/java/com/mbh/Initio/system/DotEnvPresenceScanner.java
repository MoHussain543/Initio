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
		for (String line : Files.readAllLines(dotEnvFile)) {
			ParsedLine parsed = parseLine(line);
			if (parsed == null || !parsed.key.equals(variableName)) {
				continue;
			}
			return parsed.value.isEmpty() ? KeyState.EMPTY : KeyState.HAS_VALUE;
		}
		return KeyState.NOT_FOUND;
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
		String value = trimmed.substring(separator + 1).trim();
		return new ParsedLine(key, value);
	}

	private record ParsedLine(String key, String value) {
	}
}
