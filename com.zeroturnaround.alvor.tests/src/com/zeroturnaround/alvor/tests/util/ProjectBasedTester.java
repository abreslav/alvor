package com.zeroturnaround.alvor.tests.util;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.WorkspaceUtil;
import com.zeroturnaround.alvor.crawler.StringCollector;
import com.zeroturnaround.alvor.gui.AlvorGuiPlugin;

/**
 * This is helper for WorkspaceBasedTest and others. It's not used directly by JUnit
 */
public class ProjectBasedTester {
	private static final Pattern filePattern = Pattern.compile("^(.*\\.java)|(\\.alvor)$", Pattern.CASE_INSENSITIVE);
	private final IProject project;
	private final IPath resultsFolder;
	private final boolean testChanges;
	private final boolean testMarkers;
	
	public static void runOn(String projectName, boolean testChanges, boolean testMarkers) {
		runOn(WorkspaceUtil.getProject(projectName), testChanges, testMarkers);
	}
	
	public static void runOn(IProject project, boolean testChanges, boolean testMarkers) {
		ProjectBasedTester test = new ProjectBasedTester(project, testChanges, testMarkers);
		test.testAlvorFeaturesAsRequiredByProject();
	}
	
    private ProjectBasedTester(IProject project, boolean testChanges, boolean testMarkers) {
		this.project = project;
		this.testChanges = testChanges;
		this.testMarkers = testMarkers;
		this.resultsFolder = TestUtil.getTestResultsFolder(project, null);
	}
    
    public void testAlvorFeaturesAsRequiredByProject() {
    	// initialize project
		if (this.testChanges) {
			MarkedFileChanger.undoAllChangesInProject(project, filePattern);
		}
		CacheProvider.getCache().clearProject(project.getName());
		
		// always test hotspot strings
		StringCollector.updateProjectCache(project, CacheProvider.getCache(), null);
		List<HotspotDescriptor> hotspots = (CacheProvider.getCache().getPrimaryHotspots(project.getName()));
		TestUtil.storeFoundHotspotInfo(hotspots, this.resultsFolder);
    	
		
    	if (this.testMarkers) {
    		findAndStoreAlvorMarkers(this.resultsFolder);
    	}
    	
    	// perform changes in files and store resulting markers
    	if (this.testChanges) {
    		// TODO check that it has alvor builder enabled
    		int changeNo = 1;
    		while (MarkedFileChanger.applyChangesInProject(project, filePattern, changeNo)) {
    			// TODO wait until alvor has finished
    			IPath folder = TestUtil.getTestResultsFolder(project, "change_" + changeNo);
        		findAndStoreAlvorMarkers(folder);
    			changeNo++;
    		}
    	}
    	
    	// now finally validate stuff (when all data is collected)
    	String differences = TestUtil.findDifferencesInResults(this.resultsFolder.toFile());
    	if (differences != null && !differences.isEmpty()) {
    		throw new AssertionError("Diff: " + differences);
    	}
    }
    
	private void findAndStoreAlvorMarkers(IPath folder) {
		findAndStoreMarkers(AlvorGuiPlugin.ERROR_MARKER_ID, folder);
		findAndStoreMarkers(AlvorGuiPlugin.WARNING_MARKER_ID, folder);
		findAndStoreMarkers(AlvorGuiPlugin.HOTSPOT_MARKER_ID, folder);
    }
    
    private void findAndStoreMarkers(String markerId, IPath folder) {
		List<String> markers = WorkspaceUtil.getMarkersAsStrings(this.project, markerId);
		Collections.sort(markers);
		String shortId = markerId.substring(markerId.lastIndexOf('.')+1);
		TestUtil.storeFoundTestResults(markers, folder, shortId);
    }
}
