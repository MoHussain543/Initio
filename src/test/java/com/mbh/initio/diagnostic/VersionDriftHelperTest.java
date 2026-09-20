package com.mbh.initio.diagnostic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionDriftHelperTest {

	@Test
	void readsReferenceMajorFromCommonExpressions() {
		assertEquals(20, VersionDriftHelper.referenceMajor(">=20").orElseThrow());
		assertEquals(22, VersionDriftHelper.referenceMajor("^22").orElseThrow());
		assertEquals(25, VersionDriftHelper.referenceMajor("25").orElseThrow());
	}

	@Test
	void detectsDifferentMajors() {
		assertFalse(VersionDriftHelper.sameMajor(">=20", "^22"));
		assertTrue(VersionDriftHelper.sameMajor("25", "25"));
	}
}
