package com.mbh.initio.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record ServiceRequirement(
		String serviceName,
		Path composeFile,
		String image,
		List<Integer> publishedHostPorts,
		DetectionSource source
) {
	public ServiceRequirement {
		Objects.requireNonNull(serviceName, "serviceName");
		Objects.requireNonNull(source, "source");
		publishedHostPorts = List.copyOf(Objects.requireNonNull(publishedHostPorts, "publishedHostPorts"));
	}

	public boolean composeBacked() {
		return composeFile != null;
	}

	public static ServiceRequirement configured(String serviceName, int port, DetectionSource source) {
		return new ServiceRequirement(serviceName, null, null, List.of(port), source);
	}
}
