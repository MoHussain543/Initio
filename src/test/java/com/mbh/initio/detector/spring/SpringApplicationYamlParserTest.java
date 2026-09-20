package com.mbh.initio.detector.spring;

import com.mbh.initio.detector.DetectionException;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringApplicationYamlParserTest {

	@Test
	void readsServerPortFromApplicationYaml() {
		var port = new SpringApplicationYamlParser().parseServerPort(
				FixtureRepositories.springYaml().resolve("src/main/resources/application.yml")
		);

		assertEquals(9090, port.orElseThrow());
	}

	@Test
	void malformedApplicationYamlThrowsControlledDetectionException() {
		DetectionException exception = assertThrows(
				DetectionException.class,
				() -> new SpringApplicationYamlParser().parseServerPort(
						FixtureRepositories.malformedSpringYaml().resolve("src/main/resources/application.yml")
				)
		);

		assertTrue(exception.getMessage().contains("Could not parse application.yml"));
		assertTrue(exception.getMessage().contains("Invalid YAML"));
	}
}
