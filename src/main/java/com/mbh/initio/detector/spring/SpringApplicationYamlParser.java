package com.mbh.initio.detector.spring;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SpringApplicationYamlParser {

	public Optional<Integer> parseServerPort(Path yamlFile) {
		Objects.requireNonNull(yamlFile, "yamlFile");
		try {
			Object loaded = new Yaml().load(Files.readString(yamlFile));
			return extractServerPort(loaded);
		} catch (IOException exception) {
			return Optional.empty();
		}
	}

	private static Optional<Integer> extractServerPort(Object node) {
		if (!(node instanceof Map<?, ?> map)) {
			return Optional.empty();
		}
		Object server = map.get("server");
		if (!(server instanceof Map<?, ?> serverMap)) {
			return Optional.empty();
		}
		return parsePortValue(serverMap.get("port"));
	}

	private static Optional<Integer> parsePortValue(Object port) {
		if (port instanceof Number number) {
			return validPort(number.intValue());
		}
		if (port == null) {
			return Optional.empty();
		}
		String text = port.toString().trim();
		if (text.isEmpty() || !text.chars().allMatch(Character::isDigit)) {
			return Optional.empty();
		}
		return validPort(Integer.parseInt(text));
	}

	private static Optional<Integer> validPort(int port) {
		if (port <= 0 || port > 65535) {
			return Optional.empty();
		}
		return Optional.of(port);
	}
}
