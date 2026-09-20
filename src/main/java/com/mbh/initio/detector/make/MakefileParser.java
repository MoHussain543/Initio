package com.mbh.initio.detector.make;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class MakefileParser {

	private static final Pattern TARGET_LINE = Pattern.compile("^([A-Za-z0-9_.-]+)\\s*:");
	private static final Set<String> RECOGNIZED_TARGETS = Set.of("run", "dev", "start", "test", "build", "lint");

	public List<String> parseRecognizedTargets(Path makefile) {
		Objects.requireNonNull(makefile, "makefile");
		try {
			Set<String> targets = new LinkedHashSet<>();
			for (String line : Files.readAllLines(makefile)) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(".")) {
					continue;
				}
				if (trimmed.startsWith("\t")) {
					continue;
				}
				var matcher = TARGET_LINE.matcher(trimmed);
				if (!matcher.find()) {
					continue;
				}
				String target = matcher.group(1).toLowerCase(Locale.ROOT);
				if (RECOGNIZED_TARGETS.contains(target)) {
					targets.add(target);
				}
			}
			return List.copyOf(targets);
		} catch (IOException exception) {
			return List.of();
		}
	}
}
