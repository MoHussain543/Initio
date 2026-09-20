package com.mbh.initio.projectconfig;

import com.mbh.initio.diagnostic.DiagnosticRuleId;
import com.mbh.initio.model.CommandCategory;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitioConfigValidatorTest {

	private final InitioConfigValidator validator = new InitioConfigValidator();

	@Test
	void normalizesIgnoreExpressionsIntoTypedSuppressions() {
		Object yaml = load("""
				ignore:
				  - " env: legacy_api_key "
				  - "port: 3001"
				  - "rule:DECLARATION_DRIFT"
				""");

		var parsed = validator.parse(yaml);

		assertTrue(parsed.valid());
		List<DiagnosticSuppression> suppressions = parsed.config().suppressions();
		assertEquals(new DiagnosticSuppression.Environment("LEGACY_API_KEY"), suppressions.get(0));
		assertEquals(new DiagnosticSuppression.Port(3001), suppressions.get(1));
		assertEquals(new DiagnosticSuppression.Rule(DiagnosticRuleId.DECLARATION_DRIFT), suppressions.get(2));
	}

	@Test
	void parsesConfiguredCommandCategory() {
		Object yaml = load("""
				commands:
				  - name: backend
				    category: RUN
				    command: "./scripts/start-backend.sh"
				""");

		var parsed = validator.parse(yaml);

		assertTrue(parsed.valid());
		assertEquals(CommandCategory.RUN, parsed.config().commands().getFirst().category());
	}

	@Test
	void missingCommandStringIsRejected() {
		Object yaml = load("""
				commands:
				  - name: backend
				    category: RUN
				""");

		var parsed = validator.parse(yaml);

		assertTrue(parsed.validation().formattedMessage().contains("commands[0].command"));
	}

	@Test
	void runtimesMustHaveVersions() {
		Object yaml = Map.of("runtimes", Map.of("java", "  "));

		var parsed = validator.parse(yaml);

		assertTrue(parsed.validation().formattedMessage().contains("runtimes.java"));
	}

	@Test
	void rejectsEnvironmentListInsteadOfString() {
		Object yaml = load("""
				environment:
				  required:
				    - [FOO, BAR]
				""");

		var parsed = validator.parse(yaml);

		assertFalse(parsed.valid());
		assertTrue(parsed.validation().formattedMessage().contains("environment.required[0]"));
		assertTrue(parsed.validation().formattedMessage().contains("must be a string"));
	}

	@Test
	void rejectsInvalidEnvironmentName() {
		Object yaml = load("""
				environment:
				  required:
				    - "NOT VALID"
				""");

		var parsed = validator.parse(yaml);

		assertFalse(parsed.valid());
		assertTrue(parsed.validation().formattedMessage().contains("not valid"));
	}

	@Test
	void rejectsDecimalPortWithoutTruncation() {
		Object yaml = load("""
				services:
				  - name: redis
				    port: 6379.5
				""");

		var parsed = validator.parse(yaml);

		assertFalse(parsed.valid());
		assertTrue(parsed.validation().formattedMessage().contains("services[0].port"));
		assertTrue(parsed.validation().formattedMessage().contains("integer"));
	}

	@Test
	void rejectsOutOfRangePort() {
		Object yaml = load("""
				services:
				  - name: redis
				    port: 70000
				""");

		var parsed = validator.parse(yaml);

		assertFalse(parsed.valid());
		assertTrue(parsed.validation().formattedMessage().contains("70000"));
	}

	@Test
	void rejectsUnsupportedRuntimeConstraint() {
		Object yaml = load("""
				runtimes:
				  node: ">=18 <21"
				""");

		var parsed = validator.parse(yaml);

		assertFalse(parsed.valid());
		assertTrue(parsed.validation().formattedMessage().contains("runtimes.node"));
	}

	@Test
	void rejectsRuntimeVersionThatCannotBeParsed() {
		Object yaml = load("""
				runtimes:
				  java: "99999999999999999999"
				""");

		var parsed = validator.parse(yaml);

		assertFalse(parsed.valid());
		assertTrue(parsed.validation().formattedMessage().contains("runtimes.java"));
		assertTrue(parsed.validation().formattedMessage().contains("not supported"));
	}

	@Test
	void rejectsUnknownIgnorePrefix() {
		Object yaml = load("""
				ignore:
				  - "service:redis"
				""");

		var parsed = validator.parse(yaml);

		assertFalse(parsed.valid());
		assertTrue(parsed.validation().formattedMessage().contains("Unknown suppression"));
	}

	@Test
	void rejectsUnknownRuleId() {
		Object yaml = load("""
				ignore:
				  - "rule:NOT_A_RULE"
				""");

		var parsed = validator.parse(yaml);

		assertFalse(parsed.valid());
		assertTrue(parsed.validation().formattedMessage().contains("Unknown rule"));
	}

	@Test
	void rejectsQuotedDecimalPort() {
		Object yaml = load("""
				services:
				  - name: redis
				    port: "6379.5"
				""");

		var parsed = validator.parse(yaml);

		assertFalse(parsed.valid());
		assertTrue(parsed.validation().formattedMessage().contains("not an integer"));
	}

	private static Object load(String yaml) {
		return new Yaml(new SafeConstructor(new LoaderOptions())).load(yaml);
	}
}
