package com.mbh.initio.detector.node;

public record PackageJson(
		String name,
		String enginesNode,
		boolean typescript,
		boolean react,
		boolean vite,
		boolean nextJs,
		boolean express
) {
}
