package com.mbh.initio.detector.node;

import com.mbh.initio.model.CommandCategory;
import com.mbh.initio.model.CommandOrigin;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.ProjectCommand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class NpmScriptCommands {

	private static final Map<String, CommandCategory> SCRIPT_CATEGORIES = Map.of(
			"dev", CommandCategory.DEV,
			"start", CommandCategory.RUN,
			"serve", CommandCategory.RUN,
			"preview", CommandCategory.DEV,
			"build", CommandCategory.BUILD,
			"test", CommandCategory.TEST,
			"lint", CommandCategory.OTHER,
			"format", CommandCategory.OTHER
	);

	private NpmScriptCommands() {
	}

	static List<ProjectCommand> fromScripts(Set<String> scriptNames, DetectionSource source) {
		List<ProjectCommand> commands = new ArrayList<>();
		Map<String, CommandCategory> ordered = new LinkedHashMap<>();
		for (String scriptName : scriptNames) {
			if (scriptName == null || scriptName.isBlank()) {
				continue;
			}
			ordered.putIfAbsent(scriptName, SCRIPT_CATEGORIES.getOrDefault(scriptName, CommandCategory.OTHER));
		}
		for (Map.Entry<String, CommandCategory> entry : ordered.entrySet()) {
			String scriptName = entry.getKey();
			commands.add(new ProjectCommand(
					scriptName,
					"npm run " + scriptName,
					entry.getValue(),
					CommandOrigin.DECLARED,
					source
			));
		}
		return List.copyOf(commands);
	}
}
