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
		Objects.requireNonNull(composeFile, "composeFile");
		Objects.requireNonNull(source, "source");
		publishedHostPorts = List.copyOf(Objects.requireNonNull(publishedHostPorts, "publishedHostPorts"));
	}
}
