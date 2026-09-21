package com.mbh.initio.detector.maven;

import com.mbh.initio.detector.DetectionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MavenPomParser {

	private static final String SPRING_BOOT_GROUP_ID = "org.springframework.boot";

	public MavenPom parse(Path pomFile) {
		Document document = readDocument(pomFile);
		Element project = document.getDocumentElement();
		Element parent = childElement(project, "parent");
		Element properties = childElement(project, "properties");
		Element compilerPluginConfig = findCompilerPluginConfiguration(project);
		return new MavenPom(
				childText(project, "artifactId"),
				childText(project, "name"),
				childText(project, "description"),
				childText(parent, "groupId"),
				childText(parent, "artifactId"),
				childText(properties, "java.version"),
				childText(properties, "maven.compiler.release"),
				childText(properties, "maven.compiler.source"),
				childText(compilerPluginConfig, "release"),
				childText(compilerPluginConfig, "source"),
				hasSpringBootDependency(project)
		);
	}

	private static Document readDocument(Path pomFile) {
		try (InputStream input = Files.newInputStream(pomFile)) {
			return secureDocumentBuilder().parse(input);
		} catch (Exception exception) {
			throw new DetectionException(
					"Unable to read " + pomFile.getFileName() + ": "
							+ exception.getClass().getSimpleName() + ": " + exception.getMessage(),
					exception
			);
		}
	}

	private static DocumentBuilder secureDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new ErrorHandler() {
			@Override
			public void warning(SAXParseException exception) {
			}

			@Override
			public void error(SAXParseException exception) throws SAXException {
				throw exception;
			}

			@Override
			public void fatalError(SAXParseException exception) throws SAXException {
				throw exception;
			}
		});
		return builder;
	}

	private static Element findCompilerPluginConfiguration(Element project) {
		Element build = childElement(project, "build");
		if (build == null) {
			return null;
		}
		Element inPlugins = findCompilerPluginConfigurationIn(childElement(build, "plugins"));
		if (inPlugins != null) {
			return inPlugins;
		}
		Element pluginManagement = childElement(build, "pluginManagement");
		return findCompilerPluginConfigurationIn(pluginManagement == null ? null : childElement(pluginManagement, "plugins"));
	}

	private static Element findCompilerPluginConfigurationIn(Element plugins) {
		if (plugins == null) {
			return null;
		}
		for (Element plugin : childElements(plugins, "plugin")) {
			if ("maven-compiler-plugin".equals(childText(plugin, "artifactId"))) {
				return childElement(plugin, "configuration");
			}
		}
		return null;
	}

	private static boolean hasSpringBootDependency(Element project) {
		return hasSpringBootDependencyIn(childElement(project, "dependencies"))
				|| hasSpringBootDependencyIn(dependencyManagementDependencies(project));
	}

	private static Element dependencyManagementDependencies(Element project) {
		Element dependencyManagement = childElement(project, "dependencyManagement");
		return dependencyManagement == null ? null : childElement(dependencyManagement, "dependencies");
	}

	private static boolean hasSpringBootDependencyIn(Element dependencies) {
		if (dependencies == null) {
			return false;
		}
		for (Element dependency : childElements(dependencies, "dependency")) {
			if (SPRING_BOOT_GROUP_ID.equals(childText(dependency, "groupId"))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Plain DOM child-element lookups are used instead of javax.xml.xpath: the JDK's built-in
	 * Xalan-derived XPath engine relies on dynamically instantiated internal classes and resource
	 * bundles that GraalVM native-image does not include by default, and chasing each missing
	 * reflection/resource entry individually is a losing game. Maven POMs never use namespace
	 * prefixes, so plain tag-name matching is equivalent to the local-name()-based XPath this
	 * replaces.
	 */
	private static Element childElement(Element parent, String tagName) {
		if (parent == null) {
			return null;
		}
		for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node instanceof Element element && tagName.equals(element.getTagName())) {
				return element;
			}
		}
		return null;
	}

	private static List<Element> childElements(Element parent, String tagName) {
		List<Element> elements = new ArrayList<>();
		if (parent == null) {
			return elements;
		}
		for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node instanceof Element element && tagName.equals(element.getTagName())) {
				elements.add(element);
			}
		}
		return elements;
	}

	private static String childText(Element parent, String tagName) {
		Element element = childElement(parent, tagName);
		if (element == null) {
			return null;
		}
		String text = element.getTextContent();
		if (text == null || text.isBlank()) {
			return null;
		}
		return text.trim();
	}
}
