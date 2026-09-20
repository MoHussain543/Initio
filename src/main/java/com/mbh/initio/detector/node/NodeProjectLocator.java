package com.mbh.initio.detector.node;

import com.mbh.initio.analysis.ProjectContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class NodeProjectLocator {

	static final String PACKAGE_JSON = "package.json";

	private static final List<String> NESTED_PROJECT_DIRS = List.of("frontend", "client", "web");

	private NodeProjectLocator() {
	}

	public static List<Path> locate(ProjectContext context) {
		List<Path> locations = new ArrayList<>();
		if (context.hasFile(PACKAGE_JSON)) {
			locations.add(Path.of(PACKAGE_JSON));
		}
		for (String directory : NESTED_PROJECT_DIRS) {
			Path nestedPackageJson = Path.of(directory, PACKAGE_JSON);
			if (context.hasFile(nestedPackageJson.toString())) {
				locations.add(nestedPackageJson);
			}
		}
		return List.copyOf(locations);
	}
}
