package com.zeroturnaround.alvor.configuration;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Test;

public class ConfigurationTest {

	@Test
	public void doit() throws ParserConfigurationException, TransformerException {
		String filename = "C:/alvor/ws/com.zeroturnaround.alvor.crawler/samples/example-conf.xml";
		ProjectConfiguration conf = ConfigurationManager.readFromFile(new File(filename));
		ConfigurationManager.saveToFile(conf, new File("D:/ajut/pula.xml"));
	}
}
