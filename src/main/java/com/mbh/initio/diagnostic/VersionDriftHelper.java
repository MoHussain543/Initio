package com.mbh.initio.diagnostic;

import com.mbh.initio.system.VersionMatcher;

import java.util.Optional;

public final class VersionDriftHelper {

	private VersionDriftHelper() {
	}

	public static Optional<Integer> referenceMajor(String expression) {
		if (expression == null || expression.isBlank()) {
			return Optional.empty();
		}
		String trimmed = expression.trim();
		if (trimmed.startsWith(">=")) {
			return optionalMajor(trimmed.substring(2).trim());
		}
		if (trimmed.startsWith("^") || trimmed.startsWith("~")) {
			return optionalMajor(trimmed.substring(1).trim());
		}
		return optionalMajor(trimmed);
	}

	public static boolean sameMajor(String left, String right) {
		Optional<Integer> leftMajor = referenceMajor(left);
		Optional<Integer> rightMajor = referenceMajor(right);
		if (leftMajor.isEmpty() || rightMajor.isEmpty()) {
			return true;
		}
		return leftMajor.get().equals(rightMajor.get());
	}

	private static Optional<Integer> optionalMajor(String value) {
		Integer major = VersionMatcher.majorVersion(value);
		return major == null ? Optional.empty() : Optional.of(major);
	}
}
