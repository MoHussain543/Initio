package com.mbh.initio.projectconfig;

import java.util.Objects;

public class InitioConfigException extends RuntimeException {

	public InitioConfigException(String message) {
		super(message);
	}

	public static InitioConfigException invalid(InitioConfigLoadResult result) {
		Objects.requireNonNull(result, "result");
		return new InitioConfigException(
				"""
				Initio could not analyze this project.

				%s

				Run:
				  initio config validate
				""".formatted(result.validation().formattedMessage())
		);
	}
}
