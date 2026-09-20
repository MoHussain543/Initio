package com.mbh.initio.diagnostic;

import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.VerificationState;
import com.mbh.initio.system.VersionMatcher;

import java.util.Optional;

public final class RuntimeRequirementEvaluator {

	private RuntimeRequirementEvaluator() {
	}

	public enum Outcome {
		UNVERIFIED,
		MISSING,
		INCOMPATIBLE,
		SATISFIED
	}

	public static Outcome outcome(RuntimeRequirement requirement, Optional<InstalledRuntime> installed) {
		if (installed.isEmpty() || installed.get().verificationState() == VerificationState.UNVERIFIED) {
			return Outcome.UNVERIFIED;
		}
		InstalledRuntime runtime = installed.get();
		if (runtime.status() == com.mbh.initio.model.RequirementStatus.MISSING) {
			return Outcome.MISSING;
		}
		String requiredVersion = requirement.requiredVersion();
		if (requiredVersion == null || requiredVersion.isBlank()) {
			return Outcome.SATISFIED;
		}
		return switch (VersionMatcher.match(runtime.detectedVersion(), requiredVersion)) {
			case COMPATIBLE -> Outcome.SATISFIED;
			case INCOMPATIBLE -> Outcome.INCOMPATIBLE;
			case UNKNOWN -> Outcome.UNVERIFIED;
		};
	}
}
