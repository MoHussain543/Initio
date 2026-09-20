package com.mbh.initio.analysis;

import com.mbh.initio.detector.docker.DockerComposeDetector;
import com.mbh.initio.detector.docker.DockerDetector;
import com.mbh.initio.detector.environment.EnvironmentDetector;
import com.mbh.initio.detector.github.GitHubActionsDetector;
import com.mbh.initio.detector.maven.MavenDetector;
import com.mbh.initio.detector.node.NodeDetector;
import com.mbh.initio.detector.spring.SpringConfigDetector;

public final class ProjectAnalyzers {

	private ProjectAnalyzers() {
	}

	public static ProjectAnalyzer create() {
		return new ProjectAnalyzer(
				new MavenDetector(),
				new NodeDetector(),
				new EnvironmentDetector(),
				new DockerComposeDetector(),
				new DockerDetector(),
				new SpringConfigDetector(),
				new GitHubActionsDetector()
		);
	}
}
