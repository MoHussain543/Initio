package com.mbh.initio.web.dto;

public record CommandResponse(
		String name,
		String command,
		String category,
		String origin,
		String sourceFile,
		String sourceDescription
) {
}
