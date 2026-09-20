package com.mbh.initio.web.dto;

import java.util.List;

public record IssueResponse(
		String ruleId,
		String severity,
		String title,
		String detail,
		List<String> sources,
		String suggestedAction,
		String copyText
) {
}
