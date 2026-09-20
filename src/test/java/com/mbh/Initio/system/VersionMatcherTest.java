package com.mbh.initio.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionMatcherTest {

	@Test
	void supportsGreaterThanOrEqualConstraints() {
		assertTrue(VersionMatcher.satisfies("22.14.0", ">=20"));
		assertFalse(VersionMatcher.satisfies("18.0.0", ">=20"));
	}

	@Test
	void supportsCaretConstraintsUsingMajorVersion() {
		assertTrue(VersionMatcher.satisfies("22.1.0", "^22"));
		assertFalse(VersionMatcher.satisfies("21.7.0", "^22"));
	}
}
