package com.mbh.initio.detector.docker;

import java.util.List;

public record ComposeServiceDefinition(
		String name,
		String image,
		List<Integer> publishedHostPorts
) {
}
