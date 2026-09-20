package com.mbh.initio.detector.docker;

import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockerComposeParserTest {

	private final DockerComposeParser parser = new DockerComposeParser();

	@Test
	void parsesServicesAndHostPortsFromFixture() {
		Path compose = FixtureRepositories.dockerized().resolve("docker-compose.yml");

		var services = parser.parseServices(compose);

		assertEquals(2, services.size());
		assertEquals("postgres", services.get(0).name());
		assertEquals("postgres:16", services.get(0).image());
		assertEquals(5432, services.get(0).publishedHostPorts().getFirst());
		assertEquals("redis", services.get(1).name());
		assertTrue(services.get(1).publishedHostPorts().contains(6379));
	}

	@Test
	void extractsHostPortFromStringSpecifications() {
		assertEquals(5432, DockerComposeParser.hostPortFromString("5432:5432"));
		assertEquals(5432, DockerComposeParser.hostPortFromString("127.0.0.1:5432:5432"));
		assertEquals(8080, DockerComposeParser.hostPortFromString("8080"));
	}
}
