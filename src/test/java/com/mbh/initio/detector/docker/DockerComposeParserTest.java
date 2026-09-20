package com.mbh.initio.detector.docker;

import com.mbh.initio.detector.DetectionException;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
		assertNull(DockerComposeParser.hostPortFromString("8080"));
	}

	@Test
	void quotedAndNumericShortFormsPublishHostPorts() {
		assertEquals(List.of(5432), DockerComposeParser.parsePorts(List.of("5432:5432")));
		assertEquals(List.of(5432), DockerComposeParser.parsePorts(List.of("127.0.0.1:5432:5432")));
	}

	@Test
	void containerOnlyPortsAreNotTreatedAsPublished() {
		assertTrue(DockerComposeParser.parsePorts(List.of(5432)).isEmpty());
		assertTrue(DockerComposeParser.parsePorts(List.of("3000")).isEmpty());
	}

	@Test
	void longFormUsesPublishedHostPortNotContainerTarget() {
		assertEquals(List.of(15432), DockerComposeParser.parsePorts(List.of(Map.of("target", 5432, "published", 15432))));
		assertTrue(DockerComposeParser.parsePorts(List.of(Map.of("target", 5432))).isEmpty());
		assertEquals(List.of(15432), DockerComposeParser.parsePorts(List.of(Map.of("target", 5432, "published", "15432"))));
	}

	@Test
	void malformedComposeYamlThrowsControlledDetectionException() {
		Path compose = FixtureRepositories.malformedCompose().resolve("docker-compose.yml");

		DetectionException exception = assertThrows(DetectionException.class, () -> parser.parseServices(compose));

		assertTrue(exception.getMessage().contains("Could not parse docker-compose.yml"));
		assertTrue(exception.getMessage().contains("Invalid YAML"));
		assertTrue(exception.getMessage().contains("near line"));
	}
}
