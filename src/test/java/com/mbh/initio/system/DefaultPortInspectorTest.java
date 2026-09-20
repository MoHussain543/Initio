package com.mbh.initio.system;

import com.mbh.initio.model.PortAvailability;
import com.mbh.initio.model.PortObservation;
import com.mbh.initio.model.VerificationState;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultPortInspectorTest {

	@Test
	void parsesListeningProcessFromLsofOutput() {
		CommandExecutor commandExecutor = new CommandExecutor() {
			@Override
			public CommandResult execute(List<String> command, Duration timeout) {
				return new CommandResult(
						0,
						"""
								COMMAND   PID USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
								node    12345 user   23u  IPv4 0x0      0t0  TCP *:5173 (LISTEN)
								""",
						""
				);
			}
		};

		PortObservation observation = new DefaultPortInspector(commandExecutor).inspectPorts(List.of(5173)).getFirst();

		assertEquals(PortAvailability.LISTENING, observation.availability());
		assertEquals("node", observation.occupantHint());
		assertEquals(VerificationState.VERIFIED, observation.verificationState());
	}

	@Test
	void treatsEmptyLsofResultAsFreePort() {
		CommandExecutor commandExecutor = (command, timeout) -> new CommandResult(1, "", "");

		PortObservation observation = new DefaultPortInspector(commandExecutor).inspectPorts(List.of(9090)).getFirst();

		assertEquals(PortAvailability.FREE, observation.availability());
		assertEquals(VerificationState.VERIFIED, observation.verificationState());
	}
}
