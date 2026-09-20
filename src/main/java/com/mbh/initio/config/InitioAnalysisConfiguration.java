package com.mbh.initio.config;

import com.mbh.initio.analysis.AnalysisEngine;
import com.mbh.initio.analysis.AnalysisEngines;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitioAnalysisConfiguration {

	@Bean
	AnalysisEngine analysisEngine() {
		return AnalysisEngines.createDefault();
	}
}
