package com.mbh.initio.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionMatcherTest {

	@Test
	void supportsGreaterThanOrEqualConstraints() {
		assertEquals(VersionMatcher.Result.COMPATIBLE, VersionMatcher.match("20.14.0", ">=18"));
		assertEquals(VersionMatcher.Result.INCOMPATIBLE, VersionMatcher.match("16.0.0", ">=18"));
	}

	@Test
	void supportsCaretConstraintsUsingMajorVersion() {
		assertTrue(VersionMatcher.satisfies("22.1.0", "^22"));
		assertFalse(VersionMatcher.satisfies("21.7.0", "^22"));
	}

	@Test
	void supportsExactAndXRangeMajors() {
		assertEquals(VersionMatcher.Result.COMPATIBLE, VersionMatcher.match("21.7.0", "21.x"));
		assertEquals(VersionMatcher.Result.INCOMPATIBLE, VersionMatcher.match("20.0.0", "21.x"));
	}

	@Test
	void complexAndRangeIsUnknownNeverCompatible() {
		assertEquals(VersionMatcher.Result.UNKNOWN, VersionMatcher.match("24.0.0", ">=18 <21"));
		assertFalse(VersionMatcher.satisfies("24.0.0", ">=18 <21"));
		assertFalse(VersionMatcher.isSupportedConstraint(">=18 <21"));
	}

	@Test
	void complexOrRangeIsUnknownUnlessFullySupported() {
		assertEquals(VersionMatcher.Result.UNKNOWN, VersionMatcher.match("20.0.0", ">=20 <23 || >=24"));
		assertEquals(VersionMatcher.Result.UNKNOWN, VersionMatcher.match("18.0.0", "18 || 20 || 22"));
	}

	@Test
	void oversizedVersionNumberIsUnknownRatherThanCrashing() {
		String oversized = "99999999999999999999";
		assertEquals(VersionMatcher.Result.UNKNOWN, VersionMatcher.match(oversized + ".0.0", ">=18"));
		assertEquals(VersionMatcher.Result.UNKNOWN, VersionMatcher.match("20.0.0", ">=" + oversized));
		assertEquals(null, VersionMatcher.majorVersion(oversized));
	}

	@Test
	void oversizedVersionConstraintIsNotASupportedConstraint() {
		String oversized = "99999999999999999999";
		assertFalse(VersionMatcher.isSupportedConstraint(oversized));
		assertFalse(VersionMatcher.isSupportedConstraint(">=" + oversized));
		assertFalse(VersionMatcher.isSupportedConstraint("^" + oversized));
	}
}
