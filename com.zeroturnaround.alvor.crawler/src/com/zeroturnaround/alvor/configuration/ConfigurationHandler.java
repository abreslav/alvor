package com.zeroturnaround.alvor.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigurationHandler {
	private static final String CONF_FILE_NAME = "alvor.xml";

	public static ProjectConfiguration loadProjectConfiguration(IJavaElement element) {
		IResource propsRes = getConfigurationResource(element.getJavaProject().getProject()); 
//		assert LOG.message("Loading configuration from: " + propsRes);
		try {
			return loadFromFile(propsRes.getLocation().toFile());
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't load configuration", e);
		}
	}

	public static IFile getConfigurationResource(IProject project) {
		return (IFile)project.findMember("/" + CONF_FILE_NAME);
	}

	public static ProjectConfiguration loadFromFile(File file) throws ParserConfigurationException, 
			SAXException, IOException {
		
		List<DataSourceProperties> dataSources = new ArrayList<DataSourceProperties>();
		List<HotspotProperties> hotspots = new ArrayList<HotspotProperties>();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(file);

		// ignoring the structure for simplicity -- elements may appear wherever
		NodeList hotspotNodes = doc.getElementsByTagName("hotspot");
		for (int i = 0; i < hotspotNodes.getLength(); i++) {
			Element node = (Element)hotspotNodes.item(i);
			hotspots.add (new HotspotProperties(
					node.getAttribute("className"), 
					node.getAttribute("methodName"), 
					Integer.valueOf(node.getAttribute("argumentIndex"))));
		}

		NodeList dataSourceNodes = doc.getElementsByTagName("dataSource");
		for (int i = 0; i < hotspotNodes.getLength(); i++) {
			Element node = (Element)dataSourceNodes.item(i);
			dataSources.add (new DataSourceProperties(
					node.getAttribute("pattern"), 
					node.getAttribute("driverName"), 
					node.getAttribute("url"), 
					node.getAttribute("userName"), 
					node.getAttribute("password")));
		}
		
		return new ProjectConfiguration(hotspots, dataSources, file);
	}

	public static void saveToFile(ProjectConfiguration conf, File file) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.newDocument();

		Element root = doc.createElement("alvor");
		doc.appendChild(root);

		Element hotspotNodes = doc.createElement("hotspots"); 
		root.appendChild(hotspotNodes);
		for (HotspotProperties hotspot : conf.getHotspots()) {
			Element node = doc.createElement("hotspot");
			node.setAttribute("className", hotspot.getClassName());
			node.setAttribute("methodName", hotspot.getMethodName());
			node.setAttribute("argumentIndex", String.valueOf(hotspot.getArgumentIndex()));
			hotspotNodes.appendChild(node);
		}

		Element dataSourceNodes = doc.createElement("dataSources");
		root.appendChild(dataSourceNodes);
		for (DataSourceProperties dataSource : conf.getDataSources()) {
			Element node = doc.createElement("dataSource");
			node.setAttribute("driverName", dataSource.getDriverName());
			node.setAttribute("url", dataSource.getUrl());
			node.setAttribute("username", dataSource.getUserName());
			node.setAttribute("password", dataSource.getPassword());
			dataSourceNodes.appendChild(node);
		}

		// write to file
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(file);
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
	}
}
