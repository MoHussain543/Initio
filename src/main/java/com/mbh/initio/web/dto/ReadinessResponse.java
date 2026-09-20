package com.mbh.initio.web.dto;

public record ReadinessResponse(Integer score, String status, String summary, int issueCount) {
}
