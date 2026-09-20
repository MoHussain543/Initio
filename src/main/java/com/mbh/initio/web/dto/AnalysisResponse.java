package com.mbh.initio.web.dto;

import java.util.List;

public record AnalysisResponse(
		ProjectResponse project,
		ReadinessResponse readiness,
		List<RuntimeRowResponse> runtime,
		List<EnvironmentRowResponse> environment,
		List<ServiceRowResponse> services,
		List<PortRowResponse> ports,
		List<IssueResponse> issues,
		List<CommandResponse> commands,
		List<CiResponse> ci
) {
}
