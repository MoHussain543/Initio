package com.mbh.initio.system;

import com.mbh.initio.model.EnvironmentVariableStatus;
import com.mbh.initio.model.PresenceStatus;

import java.nio.file.Path;

public final class DefaultEnvironmentProvider implements EnvironmentProvider {

	private static final String DOT_ENV = ".env";

	private final Path projectRoot;

	public DefaultEnvironmentProvider(Path projectRoot) {
		this.projectRoot = projectRoot;
	}

	@Override
	public EnvironmentVariableStatus inspect(String variableName) {
		String systemValue = System.getenv(variableName);
		if (systemValue != null) {
			PresenceStatus presence = systemValue.isBlank() ? PresenceStatus.EMPTY : PresenceStatus.PRESENT;
			return EnvironmentVariableStatus.verified(variableName, presence);
		}
		try {
			DotEnvPresenceScanner.KeyState keyState = DotEnvPresenceScanner.keyState(
					projectRoot.resolve(DOT_ENV),
					variableName
			);
			return switch (keyState) {
				case NOT_FOUND -> EnvironmentVariableStatus.verified(variableName, PresenceStatus.MISSING);
				case EMPTY -> EnvironmentVariableStatus.verified(variableName, PresenceStatus.EMPTY);
				case HAS_VALUE -> EnvironmentVariableStatus.verified(variableName, PresenceStatus.PRESENT);
			};
		} catch (Exception exception) {
			return EnvironmentVariableStatus.unverified(variableName);
		}
	}
}
