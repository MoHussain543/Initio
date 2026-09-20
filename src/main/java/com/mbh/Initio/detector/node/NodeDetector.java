package com.mbh.initio.detector.node;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;
import com.mbh.initio.detector.ProjectDetector;
import com.mbh.initio.model.DetectedTechnology;
import com.mbh.initio.model.DetectionConfidence;
import com.mbh.initio.model.DetectionSource;
import com.mbh.initio.model.ProjectMetadata;
import com.mbh.initio.model.RuntimeRequirement;
import com.mbh.initio.model.TechnologyCategory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NodeDetector implements ProjectDetector {

	static final String PACKAGE_LOCK = "package-lock.json";

	private final PackageJsonParser parser;

	public NodeDetector() {
		this(new PackageJsonParser());
	}

	NodeDetector(PackageJsonParser parser) {
		this.parser = parser;
	}

	@Override
	public boolean supports(ProjectContext context) {
		return !NodeProjectLocator.locate(context).isEmpty();
	}

	@Override
	public DetectionResult detect(ProjectContext context) {
		List<Path> packageJsonPaths = NodeProjectLocator.locate(context);
		Map<String, DetectedTechnology> technologies = new LinkedHashMap<>();
		List<RuntimeRequirement> runtimeRequirements = new ArrayList<>();
		ProjectMetadata metadata = null;

		for (Path packageJsonPath : packageJsonPaths) {
			PackageJson packageJson = parser.parse(context.resolve(packageJsonPath.toString()));
			DetectionSource source = new DetectionSource(
					packageJsonPath,
					"Declared in " + packageJsonPath,
					DetectionConfidence.HIGH
			);

			if (metadata == null && hasText(packageJson.name())) {
				metadata = new ProjectMetadata(packageJson.name(), null);
			}

			addTechnology(technologies, language(context, packageJsonPath, packageJson, source));
			if (packageJson.react()) {
				addTechnology(technologies, new DetectedTechnology("React", TechnologyCategory.FRAMEWORK, source));
			}
			if (packageJson.vite()) {
				addTechnology(technologies, new DetectedTechnology("Vite", TechnologyCategory.FRAMEWORK, source));
			}
			if (packageJson.nextJs()) {
				addTechnology(technologies, new DetectedTechnology("Next.js", TechnologyCategory.FRAMEWORK, source));
			}
			if (packageJson.express()) {
				addTechnology(technologies, new DetectedTechnology("Express", TechnologyCategory.FRAMEWORK, source));
			}
			if (hasPackageLock(context, packageJsonPath)) {
				addTechnology(technologies, new DetectedTechnology("npm", TechnologyCategory.PACKAGE_MANAGER, source));
			}

			if (hasText(packageJson.enginesNode())) {
				runtimeRequirements.add(RuntimeRequirement.declared("node", packageJson.enginesNode(), source));
			}
		}

		return new DetectionResult(metadata, List.copyOf(technologies.values()), runtimeRequirements, List.of());
	}

	private static DetectedTechnology language(
			ProjectContext context,
			Path packageJsonPath,
			PackageJson packageJson,
			DetectionSource source
	) {
		String language = usesTypeScript(context, packageJsonPath, packageJson) ? "TypeScript" : "JavaScript";
		return new DetectedTechnology(language, TechnologyCategory.LANGUAGE, source);
	}

	private static boolean usesTypeScript(ProjectContext context, Path packageJsonPath, PackageJson packageJson) {
		if (packageJson.typescript()) {
			return true;
		}
		Path packageDirectory = packageJsonPath.getParent();
		String tsconfig = packageDirectory == null
				? "tsconfig.json"
				: packageDirectory.resolve("tsconfig.json").toString();
		return context.hasFile(tsconfig);
	}

	private static boolean hasPackageLock(ProjectContext context, Path packageJsonPath) {
		Path packageDirectory = packageJsonPath.getParent();
		String lockFile = packageDirectory == null
				? PACKAGE_LOCK
				: packageDirectory.resolve(PACKAGE_LOCK).toString();
		return context.hasFile(lockFile);
	}

	private static void addTechnology(Map<String, DetectedTechnology> technologies, DetectedTechnology technology) {
		String key = technology.category().name() + ":" + technology.name();
		technologies.putIfAbsent(key, technology);
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
