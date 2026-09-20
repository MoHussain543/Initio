package com.mbh.initio.projectconfig;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class InitioConfigLoader {

	static final String PRIMARY_FILE = "initio.yml";
	static final String ALTERNATE_FILE = "initio.yaml";

	private final InitioConfigValidator validator;

	public InitioConfigLoader() {
		this(new InitioConfigValidator());
	}

	InitioConfigLoader(InitioConfigValidator validator) {
		this.validator = Objects.requireNonNull(validator, "validator");
	}

	public InitioConfigLoadResult load(Path projectRoot) {
		Objects.requireNonNull(projectRoot, "projectRoot");
		Path root = projectRoot.toAbsolutePath().normalize();
		Path primary = root.resolve(PRIMARY_FILE);
		Path alternate = root.resolve(ALTERNATE_FILE);
		boolean primaryExists = Files.isRegularFile(primary);
		boolean alternateExists = Files.isRegularFile(alternate);
		if (primaryExists && alternateExists) {
			return InitioConfigLoadResult.invalid(
					primary,
					new ConfigValidationResult(List.of(new ConfigIssue(
							PRIMARY_FILE,
							"Found both initio.yml and initio.yaml. Keep only one configuration file."
					)))
			);
		}
		if (!primaryExists && !alternateExists) {
			return InitioConfigLoadResult.notFound();
		}
		Path file = primaryExists ? primary : alternate;
		return loadFile(file);
	}

	private InitioConfigLoadResult loadFile(Path file) {
		String content;
		try {
			content = Files.readString(file);
		} catch (IOException exception) {
			return InitioConfigLoadResult.invalid(
					file,
					new ConfigValidationResult(List.of(new ConfigIssue(
							file.getFileName().toString(),
							"Unable to read the configuration file."
					)))
			);
		}
		Object loaded;
		try {
			loaded = yaml().load(content);
		} catch (YAMLException exception) {
			return InitioConfigLoadResult.invalid(
					file,
					new ConfigValidationResult(List.of(new ConfigIssue(
							file.getFileName().toString(),
							"The file is not valid YAML."
					)))
			);
		}
		InitioConfigValidator.ConfigParseResult parsed = validator.parse(loaded);
		if (!parsed.valid()) {
			return InitioConfigLoadResult.invalid(file, parsed.validation());
		}
		return InitioConfigLoadResult.valid(file, parsed.config());
	}

	private static Yaml yaml() {
		return new Yaml(new SafeConstructor(new LoaderOptions()));
	}
}
