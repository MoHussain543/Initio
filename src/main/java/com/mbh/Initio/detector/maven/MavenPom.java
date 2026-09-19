package com.mbh.Initio.detector.maven;

public record MavenPom(
		String artifactId,
		String name,
		String description,
		String parentGroupId,
		String parentArtifactId,
		String javaVersionProperty,
		String compilerReleaseProperty,
		String compilerSourceProperty,
		String compilerPluginRelease,
		String compilerPluginSource,
		boolean springBootDependency
) {
}
