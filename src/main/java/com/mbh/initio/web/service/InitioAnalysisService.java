package com.mbh.initio.web.service;

import com.mbh.initio.analysis.AnalysisEngine;
import com.mbh.initio.analysis.AnalysisResult;
import com.mbh.initio.config.InitioDashboardProperties;
import org.springframework.stereotype.Service;

@Service
public class InitioAnalysisService {

	private final AnalysisEngine analysisEngine;
	private final InitioDashboardProperties dashboardProperties;

	public InitioAnalysisService(AnalysisEngine analysisEngine, InitioDashboardProperties dashboardProperties) {
		this.analysisEngine = analysisEngine;
		this.dashboardProperties = dashboardProperties;
	}

	public AnalysisResult analyze() {
		return analysisEngine.run(dashboardProperties.resolvedProjectPath());
	}
}
