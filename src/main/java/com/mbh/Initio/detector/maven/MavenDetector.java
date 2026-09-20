package com.mbh.initio.detector.maven;

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
import java.util.List;

public final class MavenDetector implements ProjectDetector {

	static final String POM_FILE = "pom.xml";
	static final String WRAPPER_UNIX = "mvnw";
	static final String WRAPPER_WINDOWS = "mvnw.cmd";
	private static final String SPRING_BOOT_GROUP_ID = "org.springframework.boot";
	private static final String SPRING_BOOT_PARENT_ARTIFACT = "spring-boot-starter-parent";

	private final MavenPomParser parser;

	public MavenDetector() {
		this(new MavenPomParser());
	}

	MavenDetector(MavenPomParser parser) {
		this.parser = parser;
	}

	@Override
	public boolean supports(ProjectContext context) {
		return context.hasFile(POM_FILE);
	}

	@Override
	public DetectionResult detect(ProjectContext context) {
		MavenPom pom = parser.parse(context.resolve(POM_FILE));
		DetectionSource pomSource = new DetectionSource(
				Path.of(POM_FILE),
				"Declared in pom.xml",
				DetectionConfidence.HIGH
		);

		List<DetectedTechnology> technologies = new ArrayList<>();
		technologies.add(new DetectedTechnology("Java", TechnologyCategory.LANGUAGE, pomSource));
		technologies.add(new DetectedTechnology("Maven", TechnologyCategory.BUILD_TOOL, pomSource));
		if (isSpringBoot(pom)) {
			technologies.add(new DetectedTechnology("Spring Boot", TechnologyCategory.FRAMEWORK, pomSource));
		}
		if (context.hasFile(WRAPPER_UNIX) || context.hasFile(WRAPPER_WINDOWS)) {
			String wrapperFile = context.hasFile(WRAPPER_UNIX) ? WRAPPER_UNIX : WRAPPER_WINDOWS;
			technologies.add(new DetectedTechnology(
					"Maven Wrapper",
					TechnologyCategory.TOOL,
					new DetectionSource(Path.of(wrapperFile), "Maven Wrapper script", DetectionConfidence.HIGH)
			));
		}

		List<RuntimeRequirement> requirements = new ArrayList<>();
		String javaVersion = declaredJavaVersion(pom);
		if (javaVersion != null) {
			requirements.add(RuntimeRequirement.declared("java", javaVersion, pomSource));
		}

		return new DetectionResult(metadata(pom), technologies, requirements);
	}

	private static boolean isSpringBoot(MavenPom pom) {
		return (SPRING_BOOT_GROUP_ID.equals(pom.parentGroupId())
				&& SPRING_BOOT_PARENT_ARTIFACT.equals(pom.parentArtifactId()))
				|| pom.springBootDependency();
	}

	private static String declaredJavaVersion(MavenPom pom) {
		if (hasText(pom.javaVersionProperty())) {
			return pom.javaVersionProperty();
		}
		if (hasText(pom.compilerReleaseProperty())) {
			return pom.compilerReleaseProperty();
		}
		if (hasText(pom.compilerSourceProperty())) {
			return pom.compilerSourceProperty();
		}
		if (hasText(pom.compilerPluginRelease())) {
			return pom.compilerPluginRelease();
		}
		if (hasText(pom.compilerPluginSource())) {
			return pom.compilerPluginSource();
		}
		return null;
	}

	private static ProjectMetadata metadata(MavenPom pom) {
		if (hasText(pom.name())) {
			return new ProjectMetadata(pom.name(), pom.description());
		}
		if (hasText(pom.artifactId())) {
			return new ProjectMetadata(pom.artifactId(), pom.description());
		}
		return null;
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
