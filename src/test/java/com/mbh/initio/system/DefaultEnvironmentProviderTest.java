package com.mbh.initio.system;

import com.mbh.initio.model.PresenceStatus;
import com.mbh.initio.model.VerificationState;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultEnvironmentProviderTest {

	@Test
	void reportsMissingVariablesWhenDotEnvIsAbsent() {
		EnvironmentProvider provider = new DefaultEnvironmentProvider(FixtureRepositories.brokenEnv());

		var status = provider.inspect("JWT_SECRET");

		assertEquals(VerificationState.VERIFIED, status.verificationState());
		assertEquals(PresenceStatus.MISSING, status.presence());
	}

	@Test
	void reportsPresentWhenKeyExistsWithValueInDotEnv(@TempDir Path tempDir) throws Exception {
		Files.writeString(tempDir.resolve(".env"), "JWT_SECRET=local-secret\n");

		EnvironmentProvider provider = new DefaultEnvironmentProvider(tempDir);

		var status = provider.inspect("JWT_SECRET");

		assertEquals(PresenceStatus.PRESENT, status.presence());
	}

	@Test
	void reportsEmptyWhenKeyExistsWithoutValue(@TempDir Path tempDir) throws Exception {
		Files.writeString(tempDir.resolve(".env"), "JWT_SECRET=\n");

		EnvironmentProvider provider = new DefaultEnvironmentProvider(tempDir);

		var status = provider.inspect("JWT_SECRET");

		assertEquals(PresenceStatus.EMPTY, status.presence());
	}
}
