package com.mbh.initio.web.dto;

public record ProjectConfigurationResponse(
		boolean present,
		String file,
		int environmentRequired,
		int runtimes,
		int commands,
		int services,
		int ignore
) {
	public static ProjectConfigurationResponse absent() {
		return new ProjectConfigurationResponse(false, null, 0, 0, 0, 0, 0);
	}
}
