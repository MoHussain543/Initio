package com.mbh.initio.web.dto;

import java.util.List;

public record CiResponse(
		String workflowFile,
		List<String> javaVersions,
		List<String> nodeVersions,
		List<String> runCommands
) {
}
