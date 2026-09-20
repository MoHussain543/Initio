package com.mbh.initio.detector.node;

import java.util.Set;

public record PackageJson(
		String name,
		String enginesNode,
		Set<String> scriptNames,
		boolean typescript,
		boolean react,
		boolean vite,
		boolean nextJs,
		boolean express
) {
	public PackageJson {
		scriptNames = Set.copyOf(scriptNames);
	}
}
