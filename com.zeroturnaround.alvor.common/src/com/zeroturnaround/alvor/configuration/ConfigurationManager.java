package com.zeroturnaround.alvor.configuration;

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

import com.zeroturnaround.alvor.common.AlvorCommonPlugin;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

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
		
		List<DataSourceProperties> dataSources = new ArrayList<DataSourceProperties>();
		List<HotspotPattern> hotspots = new ArrayList<HotspotPattern>();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(stream);

		// ignoring the structure for simplicity -- elements may appear wherever
		NodeList hotspotNodes = doc.getElementsByTagName("hotspot");
		for (int i = 0; i < hotspotNodes.getLength(); i++) {
			Element node = (Element)hotspotNodes.item(i);
			hotspots.add (new HotspotPattern(
					node.getAttribute("className"),
					node.getAttribute("methodName"), 
					Integer.valueOf(node.getAttribute("argumentIndex"))));
		}

		NodeList dataSourceNodes = doc.getElementsByTagName("dataSource");
		for (int i = 0; i < dataSourceNodes.getLength(); i++) {
			Element node = (Element)dataSourceNodes.item(i);
			dataSources.add (new DataSourceProperties(
					node.getAttribute("pattern"), 
					node.getAttribute("driverName"), 
					node.getAttribute("url"), 
					node.getAttribute("userName"), 
					node.getAttribute("password")));
		}
		
		// get attributes
		Map<String, String> attributes = new HashMap<String, String>();
		NamedNodeMap nnm = doc.getDocumentElement().getAttributes();
		for (int i = 0; i < nnm.getLength(); i++) {
			attributes.put(nnm.item(i).getNodeName(), nnm.item(i).getNodeValue());
		}

		return new ProjectConfiguration(hotspots, dataSources, attributes);
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
		for (HotspotPattern hotspot : conf.getHotspots()) {
			Element node = doc.createElement("hotspot");
			node.setAttribute("className", hotspot.getClassName());
			node.setAttribute("methodName", hotspot.getMethodName());
			node.setAttribute("argumentIndex", String.valueOf(hotspot.getArgumentIndex()));
			hotspotNodes.appendChild(node);
		}

		Element dataSourceNodes = doc.createElement("dataSources");
		docElement.appendChild(dataSourceNodes);
		for (DataSourceProperties dataSource : conf.getDataSources()) {
			Element node = doc.createElement("dataSource");
			node.setAttribute("pattern", dataSource.getPattern());
			node.setAttribute("driverName", dataSource.getDriverName());
			node.setAttribute("url", dataSource.getUrl());
			node.setAttribute("userName", dataSource.getUserName());
			node.setAttribute("password", dataSource.getPassword());
			dataSourceNodes.appendChild(node);
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
