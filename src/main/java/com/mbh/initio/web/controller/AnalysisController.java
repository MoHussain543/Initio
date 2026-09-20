package com.mbh.initio.web.controller;

import com.mbh.initio.web.dto.AnalysisResponse;
import com.mbh.initio.web.mapper.AnalysisResponseMapper;
import com.mbh.initio.web.service.InitioAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AnalysisController {

	private final InitioAnalysisService analysisService;
	private final AnalysisResponseMapper responseMapper;

	public AnalysisController(InitioAnalysisService analysisService, AnalysisResponseMapper responseMapper) {
		this.analysisService = analysisService;
		this.responseMapper = responseMapper;
	}

	@GetMapping("/analysis")
	public AnalysisResponse analysis() {
		return responseMapper.toResponse(analysisService.analyze());
	}
}
