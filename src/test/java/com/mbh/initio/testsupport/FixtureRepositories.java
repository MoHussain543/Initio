package com.mbh.initio.testsupport;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public final class FixtureRepositories {

	private FixtureRepositories() {
	}

	public static Path springMaven() {
		return resolve("spring-maven");
	}

	public static Path mavenPlain() {
		return resolve("maven-plain");
	}

	public static Path mavenNoJavaVersion() {
		return resolve("maven-no-java-version");
	}

	public static Path empty() {
		return resolve("empty");
	}

	public static Path mavenCompilerPlugin() {
		return resolve("maven-compiler-plugin");
	}

	public static Path mavenMalformed() {
		return resolve("maven-malformed");
	}

	public static Path nodeVite() {
		return resolve("node-vite");
	}

	public static Path fullstack() {
		return resolve("fullstack");
	}

	public static Path brokenEnv() {
		return resolve("broken-env");
	}

	public static Path dockerized() {
		return resolve("dockerized");
	}

	public static Path ciJavaDrift() {
		return resolve("ci-java-drift");
	}

	public static Path springYaml() {
		return resolve("spring-yaml");
	}

	public static Path makeTargets() {
		return resolve("make-targets");
	}

	public static Path withInitioConfig() {
		return resolve("with-initio-config");
	}

	public static Path invalidInitioConfig() {
		return resolve("invalid-initio-config");
	}

	public static Path configRuntimeConflict() {
		return resolve("config-runtime-conflict");
	}

	public static Path configSuppression() {
		return resolve("config-suppression");
	}

	public static Path malformedCompose() {
		return resolve("malformed-compose");
	}

	public static Path malformedSpringYaml() {
		return resolve("malformed-spring-yaml");
	}

	public static Path malformedGitHubActionsYaml() {
		return resolve("malformed-gha-yaml");
	}

	public static Path githubMatrix() {
		return resolve("github-matrix");
	}

	public static Path githubUnresolved() {
		return resolve("github-unresolved");
	}

	private static Path resolve(String name) {
		URL resource = FixtureRepositories.class.getResource("/fixtures/" + name);
		if (resource == null) {
			throw new IllegalStateException("Missing test fixture: " + name);
		}
		try {
			return Path.of(resource.toURI());
		} catch (URISyntaxException exception) {
			throw new IllegalStateException("Invalid test fixture path: " + name, exception);
		}
	}
}
