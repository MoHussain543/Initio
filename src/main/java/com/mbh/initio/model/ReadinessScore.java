package com.mbh.initio.model;

public record ReadinessScore(
		int percent,
		int verifiedPassed,
		int verifiedTotal,
		int unverifiedCount,
		String summary
) {
}
