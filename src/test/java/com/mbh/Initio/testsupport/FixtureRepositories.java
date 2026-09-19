package com.mbh.Initio.testsupport;

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
