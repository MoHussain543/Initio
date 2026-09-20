package com.mbh.initio.web.dto;

public record RuntimeRowResponse(
		String runtime,
		String requiredVersion,
		String installedVersion,
		String status,
		String label,
		String source
) {
}
