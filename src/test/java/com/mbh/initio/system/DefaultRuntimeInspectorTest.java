package com.mbh.initio.system;

import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.RequirementStatus;
import com.mbh.initio.model.VerificationState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRuntimeInspectorTest {

	@Test
	void parsesQuotedOpenJdkVersion() {
		assertEquals("25.0.2", DefaultRuntimeInspector.parseJavaVersion("openjdk version \"25.0.2\" 2026-01-20").orElseThrow());
	}

	@Test
	void parsesModernJavaDistributionLine() {
		assertEquals("25.0.2", DefaultRuntimeInspector.parseJavaVersion("java 25.0.2 2026-01-20 LTS").orElseThrow());
	}

	@Test
	void parsesModernOpenJdkDistributionLine() {
		assertEquals("25.0.2", DefaultRuntimeInspector.parseJavaVersion("openjdk 25.0.2 2026-01-20").orElseThrow());
	}

	@Test
	void parsesLegacyQuotedJavaVersion() {
		assertEquals("1.8.0_402", DefaultRuntimeInspector.parseJavaVersion("java version \"1.8.0_402\"").orElseThrow());
	}

	@Test
	void parsesQuotedOpenJdkVersionWithoutDistributionPrefixOnSameToken() {
		assertEquals("21.0.2", DefaultRuntimeInspector.parseJavaVersion("openjdk version \"21.0.2\"").orElseThrow());
	}

	@Test
	void unexpectedOutputIsUnverifiedNotACrash() {
		assertTrue(DefaultRuntimeInspector.parseJavaVersion("not a java runtime").isEmpty());

		RuntimeInspector inspector = new DefaultRuntimeInspector((command, timeout) -> new CommandResult(
				0,
				"garbage",
				""
		));
		InstalledRuntime runtime = inspector.inspectJava();
		assertEquals(VerificationState.UNVERIFIED, runtime.verificationState());
	}

	@Test
	void parsesInstalledJavaVersionFromInspector() {
		RuntimeInspector inspector = new DefaultRuntimeInspector((command, timeout) -> new CommandResult(
				0,
				"java 25.0.2 2026-01-20 LTS",
				""
		));

		InstalledRuntime runtime = inspector.inspectJava();

		assertEquals(RequirementStatus.AVAILABLE, runtime.status());
		assertEquals(VerificationState.VERIFIED, runtime.verificationState());
		assertEquals("25.0.2", runtime.detectedVersion());
	}

	@Test
	void treatsMissingCommandAsMissingRuntime() {
		RuntimeInspector inspector = new DefaultRuntimeInspector((command, timeout) -> new CommandResult(
				127,
				"",
				"command not found"
		));

		InstalledRuntime runtime = inspector.inspectNode();

		assertEquals(RequirementStatus.MISSING, runtime.status());
		assertEquals(VerificationState.VERIFIED, runtime.verificationState());
	}

	@Test
	void treatsTimeoutAsUnverified() {
		RuntimeInspector inspector = new DefaultRuntimeInspector((command, timeout) -> new CommandResult(
				-1,
				"",
				"Command timed out"
		));

		InstalledRuntime runtime = inspector.inspectJava();

		assertEquals(VerificationState.UNVERIFIED, runtime.verificationState());
	}
}
