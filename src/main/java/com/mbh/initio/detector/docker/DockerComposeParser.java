package com.mbh.initio.detector.docker;

import com.mbh.initio.detector.DetectionException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DockerComposeParser {

	public List<ComposeServiceDefinition> parseServices(Path composeFile) {
		Objects.requireNonNull(composeFile, "composeFile");
		try {
			String content = Files.readString(composeFile);
			Object loaded = new Yaml().load(content);
			if (!(loaded instanceof Map<?, ?> root)) {
				return List.of();
			}
			Object servicesNode = root.get("services");
			if (!(servicesNode instanceof Map<?, ?> services)) {
				return List.of();
			}
			List<ComposeServiceDefinition> definitions = new ArrayList<>();
			for (Map.Entry<?, ?> entry : services.entrySet()) {
				String serviceName = String.valueOf(entry.getKey());
				if (!(entry.getValue() instanceof Map<?, ?> serviceMap)) {
					continue;
				}
				String image = text(serviceMap.get("image"));
				List<Integer> ports = parsePorts(serviceMap.get("ports"));
				definitions.add(new ComposeServiceDefinition(serviceName, image, ports));
			}
			return List.copyOf(definitions);
		} catch (IOException exception) {
			throw new DetectionException("Unable to read " + composeFile.getFileName() + ".", exception);
		}
	}

	private static String text(Object value) {
		if (value == null) {
			return null;
		}
		String text = String.valueOf(value).trim();
		return text.isEmpty() ? null : text;
	}

	private static List<Integer> parsePorts(Object portsNode) {
		if (portsNode == null) {
			return List.of();
		}
		Set<Integer> ports = new LinkedHashSet<>();
		if (portsNode instanceof List<?> entries) {
			for (Object entry : entries) {
				parsePortEntry(entry, ports);
			}
		} else {
			parsePortEntry(portsNode, ports);
		}
		return List.copyOf(ports);
	}

	private static void parsePortEntry(Object entry, Set<Integer> ports) {
		if (entry instanceof Number number) {
			ports.add(number.intValue());
			return;
		}
		if (entry instanceof Map<?, ?> map) {
			Object published = map.get("published");
			if (published instanceof Number publishedNumber) {
				ports.add(publishedNumber.intValue());
			}
			return;
		}
		if (entry == null) {
			return;
		}
		Integer hostPort = hostPortFromString(String.valueOf(entry));
		if (hostPort != null) {
			ports.add(hostPort);
		}
	}

	static Integer hostPortFromString(String specification) {
		String trimmed = specification.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		String[] segments = trimmed.split(":");
		if (segments.length == 1) {
			return parsePortSegment(segments[0]);
		}
		if (segments.length == 2) {
			return parsePortSegment(segments[0]);
		}
		return parsePortSegment(segments[segments.length - 2]);
	}

	private static Integer parsePortSegment(String segment) {
		String digits = segment.trim();
		if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
			return null;
		}
		int port = Integer.parseInt(digits);
		return port > 0 && port <= 65535 ? port : null;
	}
}
