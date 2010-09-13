package com.zeroturnaround.alvor.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class OptionLoader {

	private final static ILog LOG = Logs.getLog(OptionLoader.class);
	
	public static Map<String, String> getElementSqlCheckerProperties(IJavaElement element)
			throws FileNotFoundException, IOException {
		IJavaProject project = element.getJavaProject();
		IResource propsRes = getElementSqlCheckerPropertiesRes(project); 
		File propsFile = propsRes.getLocation().toFile();
		assert LOG.message("PROPS_FILE: " + propsFile);
		FileInputStream in = new FileInputStream(propsFile);
		Properties props = new Properties();
		props.load(in);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Map<String, String> result = (Map)props;
		result.put("SourceFileName", propsRes.getFullPath().toPortableString());
		return result;
	}

	public static IResource getElementSqlCheckerPropertiesRes(IJavaProject project) {
		return project.getProject().findMember("/sqlchecker.properties");
		//return project.getResource().getLocation().append("sqlchecker.properties");
	}

}
