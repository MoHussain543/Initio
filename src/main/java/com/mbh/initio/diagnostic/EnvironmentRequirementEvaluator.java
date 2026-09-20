package com.mbh.initio.diagnostic;

import com.mbh.initio.model.EnvironmentVariableStatus;
import com.mbh.initio.model.PresenceStatus;
import com.mbh.initio.model.VerificationState;

import java.util.Optional;

public final class EnvironmentRequirementEvaluator {

	public enum Outcome {
		UNVERIFIED,
		MISSING,
		EMPTY,
		SATISFIED
	}

	private EnvironmentRequirementEvaluator() {
	}

	public static Outcome outcome(Optional<EnvironmentVariableStatus> status) {
		if (status.isEmpty() || status.get().verificationState() == VerificationState.UNVERIFIED) {
			return Outcome.UNVERIFIED;
		}
		return switch (status.get().presence()) {
			case MISSING -> Outcome.MISSING;
			case EMPTY -> Outcome.EMPTY;
			case PRESENT -> Outcome.SATISFIED;
		};
	}
}
