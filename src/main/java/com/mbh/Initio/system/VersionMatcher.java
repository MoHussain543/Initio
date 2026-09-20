package com.mbh.initio.system;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionMatcher {

	private static final Pattern LEADING_NUMBER = Pattern.compile("(\\d+)");

	private VersionMatcher() {
	}

	public static boolean satisfies(String installedVersion, String requiredExpression) {
		if (installedVersion == null || installedVersion.isBlank()) {
			return false;
		}
		if (requiredExpression == null || requiredExpression.isBlank()) {
			return true;
		}
		Integer installedMajor = majorVersion(installedVersion);
		if (installedMajor == null) {
			return false;
		}
		String required = requiredExpression.trim();
		if (required.startsWith(">=")) {
			Integer minimum = majorVersion(required.substring(2).trim());
			return minimum != null && installedMajor >= minimum;
		}
		if (required.startsWith("^")) {
			Integer expected = majorVersion(required.substring(1).trim());
			return expected != null && installedMajor.equals(expected);
		}
		if (required.startsWith("~")) {
			Integer expected = majorVersion(required.substring(1).trim());
			return expected != null && installedMajor.equals(expected);
		}
		Integer exact = majorVersion(required);
		return exact != null && installedMajor.equals(exact);
	}

	static Integer majorVersion(String version) {
		Matcher matcher = LEADING_NUMBER.matcher(version);
		if (!matcher.find()) {
			return null;
		}
		return Integer.parseInt(matcher.group(1));
	}
}
