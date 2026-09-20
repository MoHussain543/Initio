package com.mbh.initio.system;

import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.RequirementStatus;
import com.mbh.initio.model.VerificationState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultRuntimeInspectorTest {

	@Test
	void parsesInstalledJavaVersion() {
		RuntimeInspector inspector = new DefaultRuntimeInspector((command, timeout) -> new CommandResult(
				0,
				"openjdk version \"25.0.2\" 2026-01-20",
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
