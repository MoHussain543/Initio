package com.mbh.initio.detector.node;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.model.CommandOrigin;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeDetectorCommandsTest {

	private final NodeDetector detector = new NodeDetector();

	@Test
	void emitsDeclaredNpmScriptCommands() {
		DetectionResult result = detector.detect(new ProjectContext(FixtureRepositories.nodeVite()));

		assertTrue(result.projectCommands().stream().anyMatch(
				command -> command.command().equals("npm run dev") && command.origin() == CommandOrigin.DECLARED
		));
		assertTrue(result.projectCommands().stream().anyMatch(
				command -> command.command().equals("npm run build")
		));
	}
}
