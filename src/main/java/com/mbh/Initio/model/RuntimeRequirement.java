package com.mbh.Initio.model;

import java.util.Objects;

public record RuntimeRequirement(
		String runtime,
		String requiredVersion,
		String detectedVersion,
		RequirementStatus status,
		DetectionSource source
) {
	public RuntimeRequirement {
		Objects.requireNonNull(runtime, "runtime");
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(source, "source");
	}

	public static RuntimeRequirement declared(String runtime, String requiredVersion, DetectionSource source) {
		return new RuntimeRequirement(runtime, requiredVersion, null, RequirementStatus.NOT_CHECKED, source);
	}
}
