package com.zeroturnaround.alvor.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class OptionLoader {
    public final static String CONF_FILE_NAME = "sqlchecker.properties";
    private final static ILog LOG = Logs.getLog(OptionLoader.class);
    
    public static Map<String, String> getElementSqlCheckerProperties(IJavaElement element)
                    throws FileNotFoundException, IOException {
            IResource propsRes = getElementSqlCheckerPropertiesRes(element.getJavaProject().getProject()); 
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

    public static IFile getElementSqlCheckerPropertiesRes(IProject project) {
        return (IFile)project.findMember("/" + OptionLoader.CONF_FILE_NAME);
    }

    public static boolean propertiesFileExists(IProject project) {
    	return project.findMember("/" + OptionLoader.CONF_FILE_NAME) != null;
    }

}
