package com.mbh.initio.web.dto;

import java.util.List;

public record ProjectResponse(String name, String path, List<TechnologyResponse> technologies) {
}
