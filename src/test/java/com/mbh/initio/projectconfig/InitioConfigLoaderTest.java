package com.mbh.initio.projectconfig;

import com.mbh.initio.diagnostic.DiagnosticRuleId;
import com.mbh.initio.model.CommandCategory;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitioConfigLoaderTest {

	private final InitioConfigLoader loader = new InitioConfigLoader();

	@Test
	void missingFileIsMissing() {
		InitioConfigLoadResult result = loader.load(FixtureRepositories.empty());

		assertTrue(result.isMissing());
	}

	@Test
	void loadsValidFixtureAndNormalizesSuppressions() {
		InitioConfigLoadResult result = loader.load(FixtureRepositories.withInitioConfig());

		assertTrue(result.isValid());
		InitioProjectConfig config = result.config();
		assertEquals(List.of("INTERNAL_API_KEY", "STRIPE_SECRET_KEY"), config.requiredEnvironmentVariables());
		assertEquals("25", config.runtimes().get("java"));
		assertEquals(">=22", config.runtimes().get("node"));
		assertEquals(1, config.commands().size());
		assertEquals("integration-tests", config.commands().getFirst().name());
		assertEquals(CommandCategory.TEST, config.commands().getFirst().category());
		assertEquals("./scripts/integration-test.sh", config.commands().getFirst().command());
		assertEquals("redis", config.services().getFirst().name());
		assertEquals(6379, config.services().getFirst().port());
		assertEquals(3, config.suppressions().size());
		assertInstanceOf(DiagnosticSuppression.Environment.class, config.suppressions().get(0));
		assertEquals("LEGACY_API_KEY", ((DiagnosticSuppression.Environment) config.suppressions().get(0)).name());
		assertInstanceOf(DiagnosticSuppression.Port.class, config.suppressions().get(1));
		assertEquals(3001, ((DiagnosticSuppression.Port) config.suppressions().get(1)).port());
		assertInstanceOf(DiagnosticSuppression.Rule.class, config.suppressions().get(2));
		assertEquals(
				DiagnosticRuleId.DECLARATION_DRIFT,
				((DiagnosticSuppression.Rule) config.suppressions().get(2)).ruleId()
		);
	}

	@Test
	void invalidCategoryProducesFieldError() {
		InitioConfigLoadResult result = loader.load(FixtureRepositories.invalidInitioConfig());

		assertTrue(result.isInvalid());
		String message = result.validation().formattedMessage();
		assertTrue(message.contains("Invalid initio.yml"));
		assertTrue(message.contains("commands[0].category"));
		assertTrue(message.contains("STARTUP"));
		assertTrue(message.contains("RUN"));
	}

	@Test
	void malformedYamlProducesControlledError(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("initio.yml"), "commands: [\n");

		InitioConfigLoadResult result = loader.load(tempDir);

		assertTrue(result.isInvalid());
		assertTrue(result.validation().formattedMessage().contains("not valid YAML"));
	}

	@Test
	void blankEnvironmentNameIsRejected(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("initio.yml"), """
				environment:
				  required:
				    - "  "
				""");

		InitioConfigLoadResult result = loader.load(tempDir);

		assertTrue(result.isInvalid());
		assertTrue(result.validation().formattedMessage().contains("environment.required[0]"));
	}

	@Test
	void invalidPortIsRejected(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("initio.yml"), """
				services:
				  - name: redis
				    port: 70000
				""");

		InitioConfigLoadResult result = loader.load(tempDir);

		assertTrue(result.isInvalid());
		assertTrue(result.validation().formattedMessage().contains("services[0].port"));
	}

	@Test
	void unknownIgnorePrefixIsRejected(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("initio.yml"), """
				ignore:
				  - "service:redis"
				""");

		InitioConfigLoadResult result = loader.load(tempDir);

		assertTrue(result.isInvalid());
		assertTrue(result.validation().formattedMessage().contains("ignore[0]"));
		assertTrue(result.validation().formattedMessage().contains("env:<NAME>"));
	}

	@Test
	void unknownRuleIdIsRejected(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("initio.yml"), """
				ignore:
				  - "rule:NOT_A_RULE"
				""");

		InitioConfigLoadResult result = loader.load(tempDir);

		assertTrue(result.isInvalid());
		assertTrue(result.validation().formattedMessage().contains("DECLARATION_DRIFT"));
	}

	@Test
	void bothYmlAndYamlIsRejected(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("initio.yml"), "commands: []");
		Files.writeString(tempDir.resolve("initio.yaml"), "commands: []");

		InitioConfigLoadResult result = loader.load(tempDir);

		assertTrue(result.isInvalid());
		assertTrue(result.validation().formattedMessage().contains("both"));
	}

	@Test
	void yamlExtensionIsAccepted(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("initio.yaml"), """
				environment:
				  required:
				    - JWT_SECRET
				""");

		InitioConfigLoadResult result = loader.load(tempDir);

		assertTrue(result.isValid());
		assertEquals("JWT_SECRET", result.config().requiredEnvironmentVariables().getFirst());
	}

	@Test
	void emptyFileIsValidEmptyConfig(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("initio.yml"), "");

		InitioConfigLoadResult result = loader.load(tempDir);

		assertTrue(result.isValid());
		assertEquals(InitioProjectConfig.empty(), result.config());
	}

	@Test
	void unknownTopLevelFieldIsRejected(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("initio.yml"), """
				docker:
				  compose: true
				""");

		InitioConfigLoadResult result = loader.load(tempDir);

		assertTrue(result.isInvalid());
		assertFalse(result.validation().valid());
		assertTrue(result.validation().formattedMessage().contains("docker"));
	}
}
