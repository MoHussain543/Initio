package com.mbh.initio.web.advice;

import com.mbh.initio.detector.DetectionException;
import com.mbh.initio.web.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InitioApiExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
		return ResponseEntity.badRequest().body(new ApiErrorResponse(
				"INVALID_PROJECT_PATH",
				safeMessage(exception, "The project path is not valid.")
		));
	}

	@ExceptionHandler(DetectionException.class)
	public ResponseEntity<ApiErrorResponse> handleDetection(DetectionException exception) {
		return ResponseEntity.badRequest().body(new ApiErrorResponse(
				"PROJECT_ANALYSIS_FAILED",
				safeMessage(exception, "The project directory could not be analyzed.")
		));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
				"INTERNAL_ERROR",
				"Initio could not complete the analysis request."
		));
	}

	private static String safeMessage(Exception exception, String fallback) {
		String message = exception.getMessage();
		if (message == null || message.isBlank()) {
			return fallback;
		}
		return message;
	}
}
