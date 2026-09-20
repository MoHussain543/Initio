package com.mbh.initio.model;

import java.util.Objects;

public record ServiceStatus(
		String serviceName,
		ServiceState state,
		VerificationState verificationState
) {
	public ServiceStatus {
		Objects.requireNonNull(serviceName, "serviceName");
		Objects.requireNonNull(verificationState, "verificationState");
	}

	public static ServiceStatus unverified(String serviceName) {
		return new ServiceStatus(serviceName, null, VerificationState.UNVERIFIED);
	}

	public static ServiceStatus running(String serviceName) {
		return new ServiceStatus(serviceName, ServiceState.RUNNING, VerificationState.VERIFIED);
	}

	public static ServiceStatus stopped(String serviceName) {
		return new ServiceStatus(serviceName, ServiceState.STOPPED, VerificationState.VERIFIED);
	}
}
