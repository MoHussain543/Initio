package com.mbh.initio.diagnostic;

import com.mbh.initio.model.PortAvailability;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortObservation;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.model.ServiceState;
import com.mbh.initio.model.ServiceStatus;
import com.mbh.initio.model.VerificationState;

import java.util.Locale;
import java.util.Optional;

public final class PortExpectationEvaluator {

	private PortExpectationEvaluator() {
	}

	public enum Outcome {
		UNVERIFIED,
		AVAILABLE,
		IN_USE_BY_EXPECTED_SERVICE,
		NOT_IN_USE,
		CONFLICT
	}

	public static Outcome outcome(
			PortExpectation expectation,
			Optional<PortObservation> observation,
			Optional<ServiceStatus> linkedService
	) {
		if (observation.isEmpty() || observation.get().verificationState() == VerificationState.UNVERIFIED) {
			return Outcome.UNVERIFIED;
		}
		PortObservation portObservation = observation.get();
		if (expectation.role() == PortRole.APPLICATION) {
			if (portObservation.availability() == PortAvailability.FREE) {
				return Outcome.AVAILABLE;
			}
			return Outcome.CONFLICT;
		}
		if (linkedService.isPresent()
				&& linkedService.get().verificationState() == VerificationState.VERIFIED
				&& linkedService.get().state() == ServiceState.RUNNING) {
			return Outcome.IN_USE_BY_EXPECTED_SERVICE;
		}
		if (portObservation.availability() == PortAvailability.FREE) {
			return Outcome.NOT_IN_USE;
		}
		if (occupantMatchesExpectation(expectation, portObservation.occupantHint())) {
			return Outcome.IN_USE_BY_EXPECTED_SERVICE;
		}
		return Outcome.CONFLICT;
	}

	static boolean occupantMatchesExpectation(PortExpectation expectation, String occupantHint) {
		if (occupantHint == null || occupantHint.isBlank()) {
			return false;
		}
		String occupant = occupantHint.toLowerCase(Locale.ROOT);
		String label = expectation.label().toLowerCase(Locale.ROOT);
		if (label.contains(occupant) || occupant.contains(firstToken(label))) {
			return true;
		}
		int parenStart = label.indexOf('(');
		if (parenStart >= 0 && label.indexOf(')', parenStart) > parenStart) {
			String image = label.substring(parenStart + 1, label.indexOf(')', parenStart));
			String imageName = image.contains(":") ? image.substring(0, image.indexOf(':')) : image;
			if (!imageName.isBlank() && (occupant.contains(imageName) || imageName.contains(occupant))) {
				return true;
			}
		}
		return false;
	}

	private static String firstToken(String label) {
		int space = label.indexOf(' ');
		if (space < 0) {
			return label;
		}
		return label.substring(0, space);
	}
}
