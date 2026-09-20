package com.mbh.initio.diagnostic;

import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.PortExpectation;
import com.mbh.initio.model.PortObservation;
import com.mbh.initio.model.PortRole;
import com.mbh.initio.model.ServiceStatus;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortExpectationEvaluatorTest {

	private static final DetectionSource SOURCE = new DetectionSource(
			Path.of("docker-compose.yml"),
			"Compose",
			DetectionConfidence.HIGH
	);

	@Test
	void treatsApplicationPortAsAvailableWhenFree() {
		PortExpectation expectation = new PortExpectation(8080, PortRole.APPLICATION, "Spring Boot application", SOURCE);

		PortExpectationEvaluator.Outcome outcome = PortExpectationEvaluator.outcome(
				expectation,
				Optional.of(PortObservation.free(8080)),
				Optional.empty()
		);

		assertEquals(PortExpectationEvaluator.Outcome.AVAILABLE, outcome);
	}

	@Test
	void flagsApplicationPortConflictWhenListening() {
		PortExpectation expectation = new PortExpectation(8080, PortRole.APPLICATION, "Spring Boot application", SOURCE);

		PortExpectationEvaluator.Outcome outcome = PortExpectationEvaluator.outcome(
				expectation,
				Optional.of(PortObservation.listening(8080, "node")),
				Optional.empty()
		);

		assertEquals(PortExpectationEvaluator.Outcome.CONFLICT, outcome);
	}

	@Test
	void treatsExpectedServicePortAsHealthyWhenComposeServiceIsRunning() {
		PortExpectation expectation = new PortExpectation(
				5432,
				PortRole.EXPECTED_SERVICE,
				"postgres (postgres:16)",
				SOURCE
		);

		PortExpectationEvaluator.Outcome outcome = PortExpectationEvaluator.outcome(
				expectation,
				Optional.of(PortObservation.free(5432)),
				Optional.of(ServiceStatus.running("postgres"))
		);

		assertEquals(PortExpectationEvaluator.Outcome.IN_USE_BY_EXPECTED_SERVICE, outcome);
	}

	@Test
	void matchesOccupantHintAgainstServiceLabel() {
		PortExpectation expectation = new PortExpectation(
				6379,
				PortRole.EXPECTED_SERVICE,
				"redis (redis:7)",
				SOURCE
		);

		assertTrue(PortExpectationEvaluator.occupantMatchesExpectation(expectation, "redis-server"));
	}
}
