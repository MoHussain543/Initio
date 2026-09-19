package com.mbh.Initio.detector.maven;

import com.mbh.Initio.detector.DetectionException;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MavenPomParser {

	private static final String PROJECT = "/*[local-name()='project']";
	private static final String SPRING_BOOT_GROUP_ID = "org.springframework.boot";

	public MavenPom parse(Path pomFile) {
		Document document = readDocument(pomFile);
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			return new MavenPom(
					text(xpath, document, PROJECT + "/*[local-name()='artifactId']"),
					text(xpath, document, PROJECT + "/*[local-name()='name']"),
					text(xpath, document, PROJECT + "/*[local-name()='description']"),
					text(xpath, document, PROJECT + "/*[local-name()='parent']/*[local-name()='groupId']"),
					text(xpath, document, PROJECT + "/*[local-name()='parent']/*[local-name()='artifactId']"),
					text(xpath, document, PROJECT + "/*[local-name()='properties']/*[local-name()='java.version']"),
					text(xpath, document, PROJECT + "/*[local-name()='properties']/*[local-name()='maven.compiler.release']"),
					text(xpath, document, PROJECT + "/*[local-name()='properties']/*[local-name()='maven.compiler.source']"),
					firstText(xpath, document, compilerPluginValue("release")),
					firstText(xpath, document, compilerPluginValue("source")),
					hasSpringBootDependency(xpath, document)
			);
		} catch (XPathExpressionException exception) {
			throw new DetectionException("Unable to read " + pomFile.getFileName() + ".", exception);
		}
	}

	private static Document readDocument(Path pomFile) {
		try (InputStream input = Files.newInputStream(pomFile)) {
			return secureDocumentBuilder().parse(input);
		} catch (Exception exception) {
			throw new DetectionException("Unable to read " + pomFile.getFileName() + ".", exception);
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
		return factory.newDocumentBuilder();
	}

	private static boolean hasSpringBootDependency(XPath xpath, Document document) throws XPathExpressionException {
		return exists(xpath, document, dependencyGroupId(PROJECT + "/*[local-name()='dependencies']"))
				|| exists(xpath, document, dependencyGroupId(PROJECT + "/*[local-name()='dependencyManagement']/*[local-name()='dependencies']"));
	}

	private static String dependencyGroupId(String dependenciesPath) {
		return dependenciesPath
				+ "/*[local-name()='dependency'][*[local-name()='groupId']='" + SPRING_BOOT_GROUP_ID + "']";
	}

	private static String[] compilerPluginValue(String configurationElement) {
		String plugin = "/*[local-name()='plugin'][*[local-name()='artifactId']='maven-compiler-plugin']"
				+ "/*[local-name()='configuration']/*[local-name()='" + configurationElement + "']";
		return new String[] {
				PROJECT + "/*[local-name()='build']/*[local-name()='plugins']" + plugin,
				PROJECT + "/*[local-name()='build']/*[local-name()='pluginManagement']/*[local-name()='plugins']" + plugin
		};
	}

	private static String firstText(XPath xpath, Document document, String... expressions) throws XPathExpressionException {
		for (String expression : expressions) {
			String value = text(xpath, document, expression);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private static String text(XPath xpath, Document document, String expression) throws XPathExpressionException {
		String value = (String) xpath.evaluate(expression, document, XPathConstants.STRING);
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private static boolean exists(XPath xpath, Document document, String expression) throws XPathExpressionException {
		return (Boolean) xpath.evaluate("boolean(" + expression + ")", document, XPathConstants.BOOLEAN);
	}
}
