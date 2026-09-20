package com.mbh.initio.web;

public final class DashboardStartupFailure extends Exception {

	private final int exitCode;

	public DashboardStartupFailure(String message, int exitCode) {
		super(message);
		this.exitCode = exitCode;
	}

	public int exitCode() {
		return exitCode;
	}
}
