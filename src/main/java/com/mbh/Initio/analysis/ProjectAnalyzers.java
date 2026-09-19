package com.mbh.Initio.analysis;

import com.mbh.Initio.detector.maven.MavenDetector;

public final class ProjectAnalyzers {

	private ProjectAnalyzers() {
	}

	public static ProjectAnalyzer create() {
		return new ProjectAnalyzer(new MavenDetector());
	}
}
