package com.googlecode.alvor.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.googlecode.alvor.common.HotspotPattern;
import com.googlecode.alvor.configuration.ConfigurationManager;
import com.googlecode.alvor.configuration.DataSourceProperties;
import com.googlecode.alvor.configuration.ProjectConfiguration;
import com.googlecode.alvor.tests.util.TestUtil;

public class ConfigurationManagerTest {
	
	@Test
	public void saveAndReadBackSame() throws Exception {
		
		File file1 = File.createTempFile("test", "tmp");
		File file2 = File.createTempFile("test", "tmp");
		try {
			ProjectConfiguration conf1 = createNormalConfiguration();
			ConfigurationManager.saveToFile(conf1, file1);
			
			ProjectConfiguration conf2 = ConfigurationManager.readFromFile(file1);
			ConfigurationManager.saveToFile(conf2, file2);
			
			// Maybe test also ProjectConfiguration.equals ?
			Assert.assertEquals(conf1, conf2);
			Assert.assertTrue(TestUtil.filesAreEqual(file1, file2));
			//printFile(file1);
			
			//Assert.f
		} finally {
			if (file1.exists()) {
				boolean success = file1.delete();
				assert success;
			}
			if (file2.exists()) {
				boolean success = file2.delete();
				assert success;
			}
		}
	}
	
	
	private ProjectConfiguration createNormalConfiguration() {
		List<HotspotPattern> hotspots = new ArrayList<HotspotPattern>();
		hotspots.add(new HotspotPattern("ClassName1", "MethodName1", "*", 1));
		hotspots.add(new HotspotPattern("ClassName2", "MethodName2", "*", 2));
		
		List<DataSourceProperties> dataSources = new ArrayList<DataSourceProperties>();
		dataSources.add(new DataSourceProperties("*", "DriverName1", "URL1", "userName1", "password1"));
		dataSources.add(new DataSourceProperties("Pattern2", "DriverName2", "URL2", "userName2", "password2"));
		
		Map<String, String> props = new HashMap<String, String>();
		props.put("prop1", "value1");
		props.put("prop2", "value2");
		
		return new ProjectConfiguration(hotspots, dataSources, props);
	}
	
}
