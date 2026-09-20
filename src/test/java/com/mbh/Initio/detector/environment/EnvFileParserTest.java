package com.mbh.initio.detector.environment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnvFileParserTest {

	@Test
	void extractsKeysWithoutRetainingValues() {
		assertEquals("DATABASE_URL", EnvFileParser.parseKey("DATABASE_URL=postgres://secret"));
		assertEquals("JWT_SECRET", EnvFileParser.parseKey("export JWT_SECRET=hidden"));
		assertNull(EnvFileParser.parseKey("# comment"));
	}

	@Test
	void readsKeysFromExampleFixture() {
		var keys = new EnvFileParser().parseKeys(
				com.mbh.initio.testsupport.FixtureRepositories.brokenEnv().resolve(".env.example")
		);

		assertEquals(2, keys.size());
		assertEquals("DATABASE_URL", keys.get(0));
		assertEquals("JWT_SECRET", keys.get(1));
	}
}
