package com.mbh.initio.projectconfig;

import java.nio.file.Path;
import java.util.Objects;

public record InitioConfigLoadResult(
		Status status,
		Path file,
		InitioProjectConfig config,
		ConfigValidationResult validation
) {
	public enum Status {
		MISSING,
		VALID,
		INVALID
	}

	public InitioConfigLoadResult {
		Objects.requireNonNull(status, "status");
		if (status != Status.MISSING) {
			Objects.requireNonNull(file, "file");
			Objects.requireNonNull(validation, "validation");
		}
		if (status == Status.VALID) {
			Objects.requireNonNull(config, "config");
		}
	}

	public static InitioConfigLoadResult notFound() {
		return new InitioConfigLoadResult(Status.MISSING, null, null, ConfigValidationResult.ok());
	}

	public static InitioConfigLoadResult valid(Path file, InitioProjectConfig config) {
		return new InitioConfigLoadResult(Status.VALID, file, config, ConfigValidationResult.ok());
	}

	public static InitioConfigLoadResult invalid(Path file, ConfigValidationResult validation) {
		return new InitioConfigLoadResult(Status.INVALID, file, null, validation);
	}

	public boolean isMissing() {
		return status == Status.MISSING;
	}

	public boolean isValid() {
		return status == Status.VALID;
	}

	public boolean isInvalid() {
		return status == Status.INVALID;
	}
}
