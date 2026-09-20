package com.mbh.initio.detector.spring;

import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringApplicationYamlParserTest {

	@Test
	void readsServerPortFromApplicationYaml() {
		var port = new SpringApplicationYamlParser().parseServerPort(
				FixtureRepositories.springYaml().resolve("src/main/resources/application.yml")
		);

		assertEquals(9090, port.orElseThrow());
	}
}
