package com.mbh.initio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.Objects;

@ConfigurationProperties(prefix = "initio.dashboard")
public class InitioDashboardProperties {

	private String projectPath;

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public Path resolvedProjectPath() {
		Objects.requireNonNull(projectPath, "initio.dashboard.project-path is not configured");
		return Path.of(projectPath).toAbsolutePath().normalize();
	}
}
