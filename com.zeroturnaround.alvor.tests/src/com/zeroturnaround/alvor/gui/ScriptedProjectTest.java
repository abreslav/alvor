package com.zeroturnaround.alvor.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.lkl.common.util.testing.LabelledParameterized;

import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.WorkspaceUtil;
import com.zeroturnaround.alvor.crawler.CrawlerTestUtil;
import com.zeroturnaround.alvor.crawler.StringCollector;
import com.zeroturnaround.alvor.gui.changetests.MarkedFileChanger;

@RunWith(value=LabelledParameterized.class)
public class ScriptedProjectTest {
	private static Pattern fileNamePattern = Pattern.compile("^(.*\\.java)|(\\.alvor)$", Pattern.CASE_INSENSITIVE);
	private IProject project;
	private List<String> storedStringTopics = new ArrayList<String>();
	
    public ScriptedProjectTest(IProject project) {
		this.project = project;
	}
    
	/**
	 * When used with JUnit, returns the projects for constructing actual test instances
	 */
	@Parameters
    public static List<Object[]> getParameters() {
    	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    	List<Object[]> parameters = new ArrayList<Object[]>();
    	
    	for (IProject project : projects) {
    		parameters.add(new Object[]{project});
    	}
        return parameters;
    }
    
    @Test
    public void performTests() throws CoreException {
    	// initialize project
		if (project.getName().contains("_changes")) {
			MarkedFileChanger.undoAllChangesInProject(project, fileNamePattern);
		}
		CacheProvider.getCache().clearProject(project.getName());
		
		// always test hotspot strings
		StringCollector.updateProjectCache(project, CacheProvider.getCache(), null);
		List<HotspotDescriptor> hotspots = (CacheProvider.getCache().getPrimaryHotspots(project.getName()));
		CrawlerTestUtil.validateNodeDescriptors(hotspots, project);
    	
		
    	if (project.getName().contains("_markers")) {
    		findAndStoreAlvorMarkers(null);
    	}
    	
    	if (project.getName().contains("_changes")) {
    		int changeNo = 1;
    		while (MarkedFileChanger.applyChangesInProject(project, fileNamePattern, changeNo)) {
    			String subfolderName = "change_" + changeNo;
        		findAndStoreAlvorMarkers(subfolderName);
    			changeNo++;
    		}
    	}
    }
    
    private void findAndStoreAlvorMarkers(String subfolderName) {
		findAndStoreMarkers(AlvorGuiPlugin.ERROR_MARKER_ID, subfolderName);
		findAndStoreMarkers(AlvorGuiPlugin.WARNING_MARKER_ID, subfolderName);
		findAndStoreMarkers(AlvorGuiPlugin.HOTSPOT_MARKER_ID, subfolderName);
    }
    
    private void findAndStoreMarkers(String markerId, String subfolderName) {
		List<String> markers = WorkspaceUtil.getMarkersAsStrings(AbstractMarkerTest.selectedProject, markerId);
		Collections.sort(markers);
		storeFoundStrings(markers, subfolderName, markerId);
    }
    
    private void validateFoundStrings() {
    	// assuming more important topics being earlier in the list
    	for (String topic : this.storedStringTopics) {
    		
    	}
    }
    
	private void storeFoundStrings(List<String> items, String subfolderName, String topic) {
		// prepare folder
		IPath folderPath = this.project.getLocation().append("AlvorSelfTestResults");
		if (subfolderName != null && !subfolderName.isEmpty() && !subfolderName.equals(".")) {
			folderPath.append(subfolderName);
		}
		boolean success = folderPath.toFile().mkdirs();
		assert success;
		
		// write strings to file
		File file = folderPath.append(topic + "_found.txt").toFile();
		if (file.exists()) {
			boolean result = file.delete();
			assert result;
		}
		try {
			PrintStream outStream = new PrintStream(file);
			for (String item : items) {
				outStream.println(item);
			}
			outStream.close();
		} 
		catch (FileNotFoundException e){
			throw new IllegalStateException(e);
		}
		
		// remember the topic
		storedStringTopics.add(subfolderName + "/" + topic);
	}
}
