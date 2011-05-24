package com.googlecode.alvor.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.googlecode.alvor.common.AlvorCommonPlugin;
import com.googlecode.alvor.common.HotspotPattern;
import com.googlecode.alvor.common.logging.ILog;
import com.googlecode.alvor.common.logging.Logs;

/*
 * 1) Loads and saves configuration files 
 * 2) Knows about configuration-file locations 
 */
public class ConfigurationManager {
	private static final String CONF_FILE_NAME = ".alvor";
	private static final String SAMPLE_CONF = "/res/default-configuration.xml"; 
	protected final static ILog LOG = Logs.getLog(ConfigurationManager.class);

	/**
	 * if customized conf file exists, then loads this. Otherwise loads default conf.
	 */
	public static ProjectConfiguration readProjectConfiguration(IProject project, boolean fallbackToDefault)  {
		try {
			InputStream stream = openProjectConfigurationStream(project, fallbackToDefault);
			try {
				return readFromStream(stream);
			} finally {
				stream.close();
			}
		} catch (Exception e) {
			LOG.error("Can't load configuration", e);
			throw new IllegalStateException(e);
		}
	}
	
	private static InputStream openDefaultConfigurationStream(IProject project) throws IOException {
		Bundle bundle = AlvorCommonPlugin.getDefault().getBundle(); 
		return FileLocator.openStream(bundle, new Path(SAMPLE_CONF), false);
	}
	
	private static InputStream openProjectConfigurationStream(IProject project, boolean fallbackToDefault) throws IOException {
		File file = getProjectConfigurationFile(project);
		
		if (file.exists()) {
			return new FileInputStream(file);
		}
		else if (fallbackToDefault) {
			return openDefaultConfigurationStream(project);
		}
		else {
			throw new FileNotFoundException("Configuration file ("+ file +") not found");
		}
	}

	public static File getProjectConfigurationFile(IProject project) {
		return project.getLocation().append("/" + CONF_FILE_NAME).toFile();
	}

	/* package */ static ProjectConfiguration readFromFile(File file) {
		try {
			InputStream stream = new FileInputStream(file);
			try {
				return readFromStream(stream);
			} finally {
				stream.close();
			}
		} catch (Exception e) {
			LOG.error("Can't load configuration", e);
			throw new IllegalStateException("Can't load configuration", e);
		}	
	}
	
	private static ProjectConfiguration readFromStream(InputStream stream) throws ParserConfigurationException, 
		SAXException, IOException {
		
		List<CheckerConfiguration> checkers = new ArrayList<CheckerConfiguration>();
		List<HotspotPattern> hotspotPatterns = new ArrayList<HotspotPattern>();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(stream);

		// ignoring the structure for simplicity -- elements may appear wherever
		NodeList hotspotNodes = doc.getElementsByTagName("hotspot");
		for (int i = 0; i < hotspotNodes.getLength(); i++) {
			Element node = (Element)hotspotNodes.item(i);
			String argTypes = node.getAttribute("argumentTypes"); 
			hotspotPatterns.add (new HotspotPattern(
					node.getAttribute("className"),
					node.getAttribute("methodName"),
					(argTypes.isEmpty() ? "*" : argTypes),
					Integer.valueOf(node.getAttribute("argumentIndex"))));
		}

		NodeList checkerNodes = doc.getElementsByTagName("checker");
		
		// old configuration files may have "dataSource" elements instead of "checker" TODO remove
		if (checkerNodes.getLength() == 0) {
			checkerNodes = doc.getElementsByTagName("dataSource");
		}
		
		for (int i = 0; i < checkerNodes.getLength(); i++) {
			Element checkerNode = (Element)checkerNodes.item(i);
			
			// collect patterns
			NodeList patternNodes = checkerNode.getElementsByTagName("pattern");
			List<String> patterns = new ArrayList<String>();
			for (int j = 0; j < patternNodes.getLength(); j++) {
				String pattern = patternNodes.item(j).getTextContent().trim();
				if (!pattern.isEmpty()) {
					patterns.add(pattern);
				}
			}
			// old configuration may have pattern as attribute // TODO remove at some point
			if (!checkerNode.getAttribute("pattern").isEmpty()) {
				patterns.add(checkerNode.getAttribute("pattern"));
			}
			
			checkers.add (new CheckerConfiguration(
					checkerNode.getAttribute("checkerName"), 
					checkerNode.getAttribute("driverName"), 
					checkerNode.getAttribute("url"), 
					checkerNode.getAttribute("userName"), 
					checkerNode.getAttribute("password"),
					patterns));
		}
		
		// get attributes
		Map<String, String> attributes = new HashMap<String, String>();
		NamedNodeMap nnm = doc.getDocumentElement().getAttributes();
		for (int i = 0; i < nnm.getLength(); i++) {
			attributes.put(nnm.item(i).getNodeName(), nnm.item(i).getNodeValue());
		}

		return new ProjectConfiguration(hotspotPatterns, checkers, attributes);
	}

	public static void saveProjectConfiguration(ProjectConfiguration conf, IProject project) {
		try {
			saveToFile(conf, getProjectConfigurationFile(project));
		} catch (Exception e) {
			LOG.error("Problem saving configuration", e);
			throw new IllegalStateException(e);
		}
	}
	
	static void saveToFile(ProjectConfiguration conf, File file) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.newDocument();

		Element docElement = doc.createElement("alvor");
		doc.appendChild(docElement);

		Element hotspotNodes = doc.createElement("hotspots"); 
		docElement.appendChild(hotspotNodes);
		for (HotspotPattern hotspot : conf.getHotspotPatterns()) {
			Element node = doc.createElement("hotspot");
			node.setAttribute("className", hotspot.getClassName());
			node.setAttribute("methodName", hotspot.getMethodName());
			node.setAttribute("argumentTypes", hotspot.getArgumentTypes());
			node.setAttribute("argumentIndex", String.valueOf(hotspot.getArgumentNo()));
			hotspotNodes.appendChild(node);
		}

		Element checkerNodes = doc.createElement("checkers");
		docElement.appendChild(checkerNodes);
		for (CheckerConfiguration checker : conf.getCheckers()) {
			Element checkerNode = doc.createElement("checker");
			checkerNode.setAttribute("checkerName", checker.getCheckerName());
			checkerNode.setAttribute("driverName", checker.getDriverName());
			checkerNode.setAttribute("url", checker.getUrl());
			checkerNode.setAttribute("userName", checker.getUserName());
			checkerNode.setAttribute("password", checker.getPassword());
			
			for (String pattern : checker.getPatterns()) {
				Element patternNode = doc.createElement("pattern");
				patternNode.setTextContent(pattern);
				checkerNode.appendChild(patternNode);
			}
			
			checkerNodes.appendChild(checkerNode);
		}
		
		for (Map.Entry<String, String> entry : conf.getProperties().entrySet()) {
			docElement.setAttribute(entry.getKey(), entry.getValue());
		}

		// write to file
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(file);
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
	}
}
