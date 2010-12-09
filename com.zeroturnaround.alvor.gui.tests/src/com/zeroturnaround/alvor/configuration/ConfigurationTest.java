package com.zeroturnaround.alvor.configuration;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class ConfigurationTest {

	@Test
	public void doit() throws ParserConfigurationException, SAXException, IOException, TransformerException {
		String filename = "C:/alvor/ws/com.zeroturnaround.alvor.crawler/samples/example-conf.xml";
		ProjectConfiguration conf = ConfigurationHandler.loadFromFile(new File(filename));
		ConfigurationHandler.saveToFile(conf, new File("D:/ajut/pula.xml"));
	}
}
