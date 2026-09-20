package com.mbh.initio.model;

import java.util.Objects;

public record PortObservation(
		int port,
		PortAvailability availability,
		String occupantHint,
		VerificationState verificationState
) {
	public PortObservation {
		Objects.requireNonNull(availability, "availability");
		Objects.requireNonNull(verificationState, "verificationState");
		if (port <= 0 || port > 65535) {
			throw new IllegalArgumentException("Invalid port: " + port);
		}
	}

	public static PortObservation unverified(int port) {
		return new PortObservation(port, PortAvailability.FREE, null, VerificationState.UNVERIFIED);
	}

	public static PortObservation free(int port) {
		return new PortObservation(port, PortAvailability.FREE, null, VerificationState.VERIFIED);
	}

	public static PortObservation listening(int port, String occupantHint) {
		return new PortObservation(port, PortAvailability.LISTENING, occupantHint, VerificationState.VERIFIED);
	}
}
