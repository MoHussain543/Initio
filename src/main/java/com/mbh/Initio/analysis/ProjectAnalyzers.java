package com.mbh.initio.analysis;

import com.mbh.initio.detector.maven.MavenDetector;

public final class ProjectAnalyzers {

	private ProjectAnalyzers() {
	}

	public static ProjectAnalyzer create() {
		return new ProjectAnalyzer(new MavenDetector());
	}
}
