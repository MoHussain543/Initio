package com.mbh.initio.model;

public record ReadinessScore(
		Integer percent,
		int verifiedPassed,
		int verifiedTotal,
		int unverifiedCount,
		String summary
) {
	public boolean scored() {
		return percent != null;
	}
}
