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

	@Test
	void scannerReturnsPresenceWithoutRetainingValues(@TempDir Path tempDir) throws Exception {
		Files.writeString(tempDir.resolve(".env"), "JWT_SECRET=super-secret-value-should-not-escape\n");

		assertEquals(DotEnvPresenceScanner.KeyState.HAS_VALUE, DotEnvPresenceScanner.keyState(tempDir.resolve(".env"), "JWT_SECRET"));
		assertEquals(DotEnvPresenceScanner.KeyState.EMPTY, DotEnvPresenceScanner.keyState(
				Files.writeString(tempDir.resolve("empty.env"), "JWT_SECRET=\n"),
				"JWT_SECRET"
		));
		assertEquals(DotEnvPresenceScanner.KeyState.NOT_FOUND, DotEnvPresenceScanner.keyState(tempDir.resolve(".env"), "MISSING"));
	}
}
