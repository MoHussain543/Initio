package com.mbh.initio.detector;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class YamlDocuments {

	private YamlDocuments() {
	}

	public static Object load(Path file) {
		Objects.requireNonNull(file, "file");
		String fileName = fileName(file);
		try {
			return yaml().load(Files.readString(file));
		} catch (IOException exception) {
			throw new DetectionException(
					"""
					Initio could not analyze this project.

					Could not parse %s:
					The file could not be read.
					""".formatted(fileName).stripTrailing(),
					exception
			);
		} catch (YAMLException exception) {
			throw new DetectionException(parseMessage(fileName, exception), exception);
		}
	}

	private static Yaml yaml() {
		return new Yaml(new SafeConstructor(new LoaderOptions()));
	}

	private static String parseMessage(String fileName, YAMLException exception) {
		if (exception instanceof MarkedYAMLException marked && marked.getProblemMark() != null) {
			int line = marked.getProblemMark().getLine() + 1;
			return """
					Initio could not analyze this project.

					Could not parse %s:
					Invalid YAML near line %d.
					""".formatted(fileName, line).stripTrailing();
		}
		return """
				Initio could not analyze this project.

				Could not parse %s:
				Invalid YAML.
				""".formatted(fileName).stripTrailing();
	}

	private static String fileName(Path file) {
		Path name = file.getFileName();
		return name == null ? file.toString() : name.toString();
	}
}
