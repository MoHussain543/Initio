package com.mbh.initio.diagnostic;

import com.mbh.initio.model.ServiceState;
import com.mbh.initio.model.ServiceStatus;
import com.mbh.initio.model.VerificationState;

import java.util.Optional;

public final class ServiceRequirementEvaluator {

	private ServiceRequirementEvaluator() {
	}

	public enum Outcome {
		UNVERIFIED,
		RUNNING,
		STOPPED
	}

	public static Outcome outcome(Optional<ServiceStatus> status) {
		if (status.isEmpty()) {
			return Outcome.UNVERIFIED;
		}
		ServiceStatus serviceStatus = status.get();
		if (serviceStatus.verificationState() == VerificationState.UNVERIFIED) {
			return Outcome.UNVERIFIED;
		}
		if (serviceStatus.state() == ServiceState.RUNNING) {
			return Outcome.RUNNING;
		}
		return Outcome.STOPPED;
	}
}
