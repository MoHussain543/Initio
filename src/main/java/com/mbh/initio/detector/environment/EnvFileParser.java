package com.mbh.initio.detector.environment;

import com.mbh.initio.detector.DetectionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EnvFileParser {

	public List<String> parseKeys(Path envFile) {
		try {
			Set<String> keys = new LinkedHashSet<>();
			for (String line : Files.readAllLines(envFile)) {
				String key = parseKey(line);
				if (key != null) {
					keys.add(key);
				}
			}
			return List.copyOf(keys);
		} catch (IOException exception) {
			throw new DetectionException("Unable to read " + envFile.getFileName() + ".", exception);
		}
	}

	static String parseKey(String line) {
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
		return key;
	}
}
