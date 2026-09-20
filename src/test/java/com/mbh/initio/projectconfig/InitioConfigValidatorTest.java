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
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitioConfigValidatorTest {

	private final InitioConfigValidator validator = new InitioConfigValidator();

	@Test
	void normalizesIgnoreExpressionsIntoTypedSuppressions() {
		Object yaml = load("""
				ignore:
				  - " env: LEGACY_API_KEY "
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

	private static Object load(String yaml) {
		return new Yaml(new SafeConstructor(new LoaderOptions())).load(yaml);
	}
}
