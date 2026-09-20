package com.mbh.initio.detector.node;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.mbh.initio.detector.DetectionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
public final class PackageJsonParser {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public PackageJson parse(Path packageJsonFile) {
		try {
			JsonNode root = MAPPER.readTree(Files.readString(packageJsonFile));
			String name = text(root.get("name"));
			String enginesNode = text(root.path("engines").path("node"));
			return new PackageJson(
					name,
					enginesNode,
					scriptNames(root.path("scripts")),
					hasDependency(root, "typescript"),
					hasDependency(root, "react"),
					hasDependency(root, "vite"),
					hasDependency(root, "next"),
					hasDependency(root, "express")
			);
		} catch (IOException exception) {
			throw new DetectionException("Unable to read " + packageJsonFile.getFileName() + ".", exception);
		}
	}

	private static boolean hasDependency(JsonNode root, String dependencyName) {
		return containsKey(root.path("dependencies"), dependencyName)
				|| containsKey(root.path("devDependencies"), dependencyName);
	}

	private static boolean containsKey(JsonNode dependencies, String dependencyName) {
		if (!dependencies.isObject()) {
			return false;
		}
		return dependencies.propertyNames().stream()
				.anyMatch(name -> name.equalsIgnoreCase(dependencyName));
	}

	private static Set<String> scriptNames(JsonNode scripts) {
		if (!scripts.isObject()) {
			return Set.of();
		}
		Set<String> names = new LinkedHashSet<>();
		for (String name : scripts.propertyNames()) {
			names.add(name);
		}
		return Set.copyOf(names);
	}

	private static String text(JsonNode node) {
		if (node == null || node.isNull() || node.isMissingNode()) {
			return null;
		}
		String value = node.asText(null);
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
