package com.mbh.initio.detector.make;

import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.model.CommandOrigin;
import com.mbh.initio.testsupport.FixtureRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MakefileDetectorTest {

	private final MakefileDetector detector = new MakefileDetector();

	@Test
	void emitsInferredMakeCommandsForRecognizedTargets() {
		var result = detector.detect(new ProjectContext(FixtureRepositories.makeTargets()));

		assertTrue(result.projectCommands().stream().anyMatch(
				command -> command.command().equals("make test") && command.origin() == CommandOrigin.INFERRED
		));
		assertTrue(result.projectCommands().stream().anyMatch(command -> command.command().equals("make run")));
	}
}
