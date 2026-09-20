package com.mbh.initio.model;

import java.util.Objects;

public record InstalledRuntime(
		String runtime,
		String detectedVersion,
		RequirementStatus status,
		VerificationState verificationState
) {
	public InstalledRuntime {
		Objects.requireNonNull(runtime, "runtime");
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(verificationState, "verificationState");
	}

	public static InstalledRuntime unverified(String runtime) {
		return new InstalledRuntime(runtime, null, RequirementStatus.NOT_CHECKED, VerificationState.UNVERIFIED);
	}

	public static InstalledRuntime missing(String runtime) {
		return new InstalledRuntime(runtime, null, RequirementStatus.MISSING, VerificationState.VERIFIED);
	}

	public static InstalledRuntime available(String runtime, String detectedVersion) {
		return new InstalledRuntime(runtime, detectedVersion, RequirementStatus.AVAILABLE, VerificationState.VERIFIED);
	}
}
