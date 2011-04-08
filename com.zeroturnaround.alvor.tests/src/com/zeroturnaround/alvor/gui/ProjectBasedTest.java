package com.zeroturnaround.alvor.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.WorkspaceUtil;
import com.zeroturnaround.alvor.crawler.StringCollector;
import com.zeroturnaround.alvor.tests.util.CrawlerTestUtil;
import com.zeroturnaround.alvor.tests.util.MarkedFileChanger;

/**
 * This is helper for WorkspaceBasedTest and others. It's not used directly by JUnit
 */
public class ProjectBasedTest {
	private static final Pattern filePattern = Pattern.compile("^(.*\\.java)|(\\.alvor)$", Pattern.CASE_INSENSITIVE);
	private final IProject project;
	private final List<String> topics = new ArrayList<String>();
	private final IPath resultsFolder;
	
	public static void runOn(IProject project) {
		ProjectBasedTest test = new ProjectBasedTest(project);
		test.testAlvorFeaturesAsRequiredByProject();
	}
	
    private ProjectBasedTest(IProject project) {
		this.project = project;
		resultsFolder = project.getLocation().append("AlvorSelfTestResults");
	}
    
    public void testAlvorFeaturesAsRequiredByProject() {
    	// initialize project
		if (project.getName().contains("_changes")) {
			MarkedFileChanger.undoAllChangesInProject(project, filePattern);
		}
		CacheProvider.getCache().clearProject(project.getName());
		
		// always test hotspot strings
		StringCollector.updateProjectCache(project, CacheProvider.getCache(), null);
		List<HotspotDescriptor> hotspots = (CacheProvider.getCache().getPrimaryHotspots(project.getName()));
		CrawlerTestUtil.validateNodeDescriptors(hotspots, project);
    	
		
    	if (project.getName().contains("_markers")) {
    		findAndStoreAlvorMarkers(null);
    	}
    	
    	// perform changes in files and store resulting markers
    	if (project.getName().contains("_changes")) {
    		// TODO check that it has alvor builder enabled
    		int changeNo = 1;
    		while (MarkedFileChanger.applyChangesInProject(project, filePattern, changeNo)) {
    			// TODO wait until alvor has finished
    			String subfolderName = "change_" + changeNo;
        		findAndStoreAlvorMarkers(subfolderName);
    			changeNo++;
    		}
    	}
    	
    	// now finally validate stuff (when all data is collected)
    	validateCollectedResults();
    }
    
    private void findAndStoreAlvorMarkers(String subfolderName) {
		findAndStoreMarkers(AlvorGuiPlugin.ERROR_MARKER_ID, subfolderName);
		findAndStoreMarkers(AlvorGuiPlugin.WARNING_MARKER_ID, subfolderName);
		findAndStoreMarkers(AlvorGuiPlugin.HOTSPOT_MARKER_ID, subfolderName);
    }
    
    private void findAndStoreMarkers(String markerId, String subfolderName) {
		List<String> markers = WorkspaceUtil.getMarkersAsStrings(AbstractMarkerTest.selectedProject, markerId);
		Collections.sort(markers);
		storeFoundTestResults(markers, subfolderName, markerId);
		topics.add(subfolderName + "/" + markerId);
    }
    
    private void validateCollectedResults() {
    	// assuming more important topics being earlier in the list
    	for (String topic : this.topics) {
    		File expectedFile = this.resultsFolder.append(topic + "_expected.txt").toFile();
    		File foundFile = this.resultsFolder.append(topic + "_found.txt").toFile();
    		if (!CrawlerTestUtil.filesAreEqual(expectedFile, foundFile)) {
    			throw new AssertionError(topic + " differs from expected");
    		}
    	}
    }
    
	private void storeFoundTestResults(List<String> items, String subfolderName, String topic) {
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
	}
}
