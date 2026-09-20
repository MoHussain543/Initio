package com.mbh.initio.diagnostic;

import com.mbh.initio.model.DiagnosticIssue;
import com.mbh.initio.model.DiagnosticSeverity;
import com.mbh.initio.projectconfig.DiagnosticSuppression;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosticSuppressionFilterTest {

	@Test
	void matchesTypedSuppressionsNotRawYamlStrings() {
		DiagnosticIssue missingLegacy = DiagnosticIssue.forEnvironment(
				DiagnosticRuleId.MISSING_ENVIRONMENT_VARIABLE,
				DiagnosticSeverity.ERROR,
				"LEGACY_API_KEY is missing",
				"Add LEGACY_API_KEY to .env",
				"LEGACY_API_KEY"
		);
		DiagnosticIssue missingJwt = DiagnosticIssue.forEnvironment(
				DiagnosticRuleId.MISSING_ENVIRONMENT_VARIABLE,
				DiagnosticSeverity.ERROR,
				"JWT_SECRET is missing",
				"Add JWT_SECRET to .env",
				"JWT_SECRET"
		);
		DiagnosticIssue drift = new DiagnosticIssue(
				DiagnosticRuleId.DECLARATION_DRIFT,
				DiagnosticSeverity.WARNING,
				"Conflicting Node engine declarations",
				"Node >=20"
		);
		DiagnosticIssue occupied = DiagnosticIssue.forPort(
				DiagnosticRuleId.PORT_CONFLICT,
				DiagnosticSeverity.ERROR,
				"Port 3001 is occupied",
				"Free port 3001",
				3001
		);
		List<DiagnosticSuppression> suppressions = List.of(
				new DiagnosticSuppression.Environment("LEGACY_API_KEY"),
				new DiagnosticSuppression.Port(3001),
				new DiagnosticSuppression.Rule(DiagnosticRuleId.DECLARATION_DRIFT)
		);

		List<DiagnosticIssue> visible = DiagnosticSuppressionFilter.visible(
				List.of(missingLegacy, missingJwt, drift, occupied),
				suppressions
		);

		assertEquals(List.of(missingJwt), visible);
		assertTrue(DiagnosticSuppressionFilter.matches(missingLegacy, suppressions.get(0)));
		assertTrue(DiagnosticSuppressionFilter.matches(occupied, suppressions.get(1)));
		assertTrue(DiagnosticSuppressionFilter.matches(drift, suppressions.get(2)));
		assertFalse(DiagnosticSuppressionFilter.matches(missingJwt, suppressions.get(0)));
	}

	@Test
	void environmentMatchIsCaseInsensitive() {
		DiagnosticIssue issue = DiagnosticIssue.forEnvironment(
				DiagnosticRuleId.MISSING_ENVIRONMENT_VARIABLE,
				DiagnosticSeverity.ERROR,
				"legacy_api_key is missing",
				"Add it",
				"legacy_api_key"
		);

		assertTrue(DiagnosticSuppressionFilter.matches(
				issue,
				new DiagnosticSuppression.Environment("LEGACY_API_KEY")
		));
	}
}
