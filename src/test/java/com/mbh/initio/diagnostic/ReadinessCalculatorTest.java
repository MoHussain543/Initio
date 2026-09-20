package com.mbh.initio.diagnostic;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.model.EnvironmentVariableRequirement;
import com.mbh.initio.model.EnvironmentVariableStatus;
import com.mbh.initio.model.InstalledRuntime;
import com.mbh.initio.model.LocalEnvironmentAnalysis;
import com.mbh.initio.model.PresenceStatus;
import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.ReadinessScore;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.projectconfig.DiagnosticSuppression;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadinessCalculatorTest {

	private static final DetectionSource SOURCE = new DetectionSource(
			Path.of("pom.xml"),
			"Declared in pom.xml",
			DetectionConfidence.HIGH
	);

	private final ReadinessCalculator calculator = new ReadinessCalculator();

	@Test
	void allPassScoresOneHundred() {
		ReadinessScore score = calculator.calculate(
				context(
						List.of(RuntimeRequirement.declared("java", "25", SOURCE)),
						List.of(),
						List.of(InstalledRuntime.available("java", "25.0.2")),
						List.of()
				),
				List.of()
		);

		assertEquals(100, score.percent());
		assertEquals(1, score.verifiedPassed());
		assertEquals(1, score.verifiedTotal());
		assertTrue(score.summary().contains("Verified: 1/1"));
	}

	@Test
	void oneCriticalFailureReducesScore() {
		ReadinessScore score = calculator.calculate(
				context(
						List.of(RuntimeRequirement.declared("java", "25", SOURCE)),
						List.of(),
						List.of(InstalledRuntime.missing("java")),
						List.of()
				),
				List.of()
		);

		assertEquals(0, score.percent());
		assertEquals(0, score.verifiedPassed());
		assertEquals(1, score.verifiedTotal());
	}

	@Test
	void allUnknownIsNotOneHundred() {
		ReadinessScore score = calculator.calculate(
				context(
						List.of(RuntimeRequirement.declared("java", "25", SOURCE)),
						List.of(),
						List.of(InstalledRuntime.unverified("java")),
						List.of()
				),
				List.of()
		);

		assertNull(score.percent());
		assertFalse(score.scored());
		assertEquals(0, score.verifiedTotal());
		assertEquals(1, score.unverifiedCount());
		assertTrue(score.summary().contains("Readiness score unavailable"));
		assertNotEquals("100", String.valueOf(score.percent()));
	}

	@Test
	void mixPassAndUnknownDoesNotClaimPerfectScore() {
		ReadinessScore score = calculator.calculate(
				context(
						List.of(
								RuntimeRequirement.declared("java", "25", SOURCE),
								RuntimeRequirement.declared("node", ">=20", SOURCE)
						),
						List.of(),
						List.of(
								InstalledRuntime.available("java", "25.0.2"),
								InstalledRuntime.unverified("node")
						),
						List.of()
				),
				List.of()
		);

		assertNotEquals(100, score.percent());
		assertEquals(1, score.verifiedPassed());
		assertEquals(1, score.verifiedTotal());
		assertEquals(1, score.unverifiedCount());
		assertTrue(score.summary().contains("could not verify"));
	}

	@Test
	void mixFailAndUnknownDoesNotTreatUnknownAsSuccess() {
		ReadinessScore score = calculator.calculate(
				context(
						List.of(
								RuntimeRequirement.declared("java", "25", SOURCE),
								RuntimeRequirement.declared("node", ">=20", SOURCE)
						),
						List.of(),
						List.of(
								InstalledRuntime.missing("java"),
								InstalledRuntime.unverified("node")
						),
						List.of()
				),
				List.of()
		);

		assertEquals(0, score.percent());
		assertEquals(1, score.verifiedTotal());
		assertEquals(1, score.unverifiedCount());
	}

	@Test
	void visibleErrorPreventsPerfectScoreWhenChecksAreOtherwiseEmpty() {
		DiagnosticIssue error = new DiagnosticIssue(
				DiagnosticRuleId.DOCKER_UNAVAILABLE,
				DiagnosticSeverity.ERROR,
				"Docker is not available",
				"Install Docker."
		);
		ReadinessScore score = calculator.calculate(
				emptyProject(),
				List.of(error)
		);

		assertEquals(0, score.percent());
		assertTrue(score.summary().contains("issue"));
	}

	@Test
	void noRequirementsIsUnknownNotOneHundred() {
		ReadinessScore score = calculator.calculate(emptyProject(), List.of());

		assertNull(score.percent());
		assertTrue(score.summary().contains("Readiness score unavailable"));
	}

	@Test
	void suppressedEnvironmentVariablesAreExcludedFromReadiness() {
		ProjectAnalysis project = project(
				List.of(),
				List.of(
						new EnvironmentVariableRequirement("JWT_SECRET", SOURCE),
						new EnvironmentVariableRequirement("LEGACY_API_KEY", SOURCE)
				)
		);
		AnalysisContext context = new AnalysisContext(
				project,
				new LocalEnvironmentAnalysis(
						List.of(),
						List.of(
								EnvironmentVariableStatus.verified("JWT_SECRET", PresenceStatus.PRESENT),
								EnvironmentVariableStatus.verified("LEGACY_API_KEY", PresenceStatus.MISSING)
						),
						List.of(),
						List.of()
				)
		);

		ReadinessScore withoutSuppression = calculator.calculate(context, List.of());
		ReadinessScore withSuppression = calculator.calculate(
				context,
				List.of(),
				List.of(new DiagnosticSuppression.Environment("LEGACY_API_KEY"))
		);

		assertEquals(50, withoutSuppression.percent());
		assertEquals(100, withSuppression.percent());
		assertFalse(withSuppression.summary().contains("issues"));
	}

	@Test
	void suppressedRuntimeRuleRemovesReadinessPenalty() {
		AnalysisContext context = context(
				List.of(RuntimeRequirement.declared("java", "25", SOURCE)),
				List.of(),
				List.of(InstalledRuntime.missing("java")),
				List.of()
		);

		ReadinessScore unsuppressed = calculator.calculate(context, List.of());
		ReadinessScore suppressed = calculator.calculate(
				context,
				List.of(),
				List.of(new DiagnosticSuppression.Rule(DiagnosticRuleId.MISSING_RUNTIME))
		);

		assertEquals(0, unsuppressed.percent());
		assertNull(suppressed.percent());
		assertEquals(0, suppressed.verifiedTotal());
	}

	private static AnalysisContext emptyProject() {
		return context(List.of(), List.of(), List.of(), List.of());
	}

	private static AnalysisContext context(
			List<RuntimeRequirement> runtimes,
			List<EnvironmentVariableRequirement> environment,
			List<InstalledRuntime> installed,
			List<EnvironmentVariableStatus> envStatus
	) {
		return new AnalysisContext(
				project(runtimes, environment),
				new LocalEnvironmentAnalysis(installed, envStatus, List.of(), List.of())
		);
	}

	private static ProjectAnalysis project(
			List<RuntimeRequirement> runtimes,
			List<EnvironmentVariableRequirement> environment
	) {
		return new ProjectAnalysis(
				Path.of("/demo"),
				new ProjectMetadata("demo", null),
				List.of(),
				runtimes,
				environment,
				List.of(),
				List.of(),
				List.of(),
				List.of()
		);
	}
}
