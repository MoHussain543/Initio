package com.mbh.initio.system;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionMatcher {

	public enum Result {
		COMPATIBLE,
		INCOMPATIBLE,
		UNKNOWN
	}

	private static final Pattern LEADING_NUMBER = Pattern.compile("(\\d+)");
	private static final Pattern ALL_NUMBERS = Pattern.compile("\\d+");
	private static final Pattern SUPPORTED_CONSTRAINT = Pattern.compile(
			"^(>=|>|<=|<|\\^|~)?\\d+(?:\\.x|(?:\\.\\d+)*)$"
	);

	private VersionMatcher() {
	}

	public static boolean satisfies(String installedVersion, String requiredExpression) {
		return match(installedVersion, requiredExpression) == Result.COMPATIBLE;
	}

	public static boolean isSupportedConstraint(String requiredExpression) {
		if (requiredExpression == null || requiredExpression.isBlank()) {
			return false;
		}
		String trimmed = requiredExpression.trim();
		if (!SUPPORTED_CONSTRAINT.matcher(trimmed).matches()) {
			return false;
		}
		return allNumbersAreParseable(trimmed);
	}

	private static boolean allNumbersAreParseable(String value) {
		Matcher matcher = ALL_NUMBERS.matcher(value);
		while (matcher.find()) {
			try {
				Integer.parseInt(matcher.group());
			} catch (NumberFormatException exception) {
				return false;
			}
		}
		return true;
	}

	public static Result match(String installedVersion, String requiredExpression) {
		if (installedVersion == null || installedVersion.isBlank()) {
			return Result.UNKNOWN;
		}
		if (requiredExpression == null || requiredExpression.isBlank()) {
			return Result.COMPATIBLE;
		}
		String required = requiredExpression.trim();
		if (!isSupportedConstraint(required)) {
			return Result.UNKNOWN;
		}
		Integer installedMajor = majorVersion(installedVersion);
		if (installedMajor == null) {
			return Result.UNKNOWN;
		}
		if (required.startsWith(">=")) {
			return compare(installedMajor, required.substring(2), true, true);
		}
		if (required.startsWith("<=")) {
			return compare(installedMajor, required.substring(2), false, true);
		}
		if (required.startsWith(">")) {
			return compare(installedMajor, required.substring(1), true, false);
		}
		if (required.startsWith("<")) {
			return compare(installedMajor, required.substring(1), false, false);
		}
		if (required.startsWith("^") || required.startsWith("~")) {
			Integer expected = majorVersion(required.substring(1));
			if (expected == null) {
				return Result.UNKNOWN;
			}
			return installedMajor.equals(expected) ? Result.COMPATIBLE : Result.INCOMPATIBLE;
		}
		Integer exact = majorVersion(required);
		if (exact == null) {
			return Result.UNKNOWN;
		}
		return installedMajor.equals(exact) ? Result.COMPATIBLE : Result.INCOMPATIBLE;
	}

	private static Result compare(int installedMajor, String bound, boolean greater, boolean inclusive) {
		Integer requiredMajor = majorVersion(bound);
		if (requiredMajor == null) {
			return Result.UNKNOWN;
		}
		int comparison = Integer.compare(installedMajor, requiredMajor);
		boolean ok = greater
				? (inclusive ? comparison >= 0 : comparison > 0)
				: (inclusive ? comparison <= 0 : comparison < 0);
		return ok ? Result.COMPATIBLE : Result.INCOMPATIBLE;
	}

	public static Integer majorVersion(String version) {
		Matcher matcher = LEADING_NUMBER.matcher(version);
		if (!matcher.find()) {
			return null;
		}
		try {
			return Integer.parseInt(matcher.group(1));
		} catch (NumberFormatException exception) {
			return null;
		}
	}
}
