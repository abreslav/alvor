package com.zeroturnaround.alvor.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class OptionLoader {

	private final static ILog LOG = Logs.getLog(OptionLoader.class);
	
	public static Map<String, Object> getElementSqlCheckerProperties(IJavaElement element)
			throws FileNotFoundException, IOException {
		IJavaProject project = element.getJavaProject();
		File propsFile = getElementSqlCheckerPropertiesFile(project);
		assert LOG.message("PROPS_FILE: " + propsFile);
		FileInputStream in = new FileInputStream(propsFile);
		Properties props = new Properties();
		props.load(in);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Map<String, Object> result = (Map)props;
		return result;
	}

	public static File getElementSqlCheckerPropertiesFile(IJavaProject project) {
		File propsFile = project.getResource().getLocation().append(
				"sqlchecker.properties").toFile();
		return propsFile;
	}

}
