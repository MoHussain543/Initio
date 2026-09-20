package com.mbh.initio.model;

import java.util.Objects;

public record EnvironmentVariableStatus(
		String name,
		PresenceStatus presence,
		VerificationState verificationState
) {
	public EnvironmentVariableStatus {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(verificationState, "verificationState");
	}

	public static EnvironmentVariableStatus unverified(String name) {
		return new EnvironmentVariableStatus(name, null, VerificationState.UNVERIFIED);
	}

	public static EnvironmentVariableStatus verified(String name, PresenceStatus presence) {
		Objects.requireNonNull(presence, "presence");
		return new EnvironmentVariableStatus(name, presence, VerificationState.VERIFIED);
	}
}
