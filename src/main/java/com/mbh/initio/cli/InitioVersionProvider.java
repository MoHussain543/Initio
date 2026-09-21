package com.mbh.initio.cli;

import picocli.CommandLine.IVersionProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class InitioVersionProvider implements IVersionProvider {

	@Override
	public String[] getVersion() {
		return new String[] {"Initio " + readVersion()};
	}

	private static String readVersion() {
		try (InputStream input = InitioVersionProvider.class.getClassLoader()
				.getResourceAsStream("META-INF/build-info.properties")) {
			if (input == null) {
				return "unknown";
			}
			Properties properties = new Properties();
			properties.load(input);
			String version = properties.getProperty("build.version");
			return version == null || version.isBlank() ? "unknown" : version;
		} catch (IOException exception) {
			return "unknown";
		}
	}
}
