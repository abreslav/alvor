package com.googlecode.alvor.tests.util;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.googlecode.alvor.builder.AlvorNature;
import com.googlecode.alvor.cache.CacheProvider;
import com.googlecode.alvor.common.HotspotDescriptor;
import com.googlecode.alvor.common.WorkspaceUtil;
import com.googlecode.alvor.gui.AlvorGuiPlugin;
import com.googlecode.alvor.gui.GuiChecker;

/**
 * This is helper for WorkspaceBasedTest and others. It's not used directly by JUnit
 */
public class ProjectBasedTester {
	public static enum TestScenario {CLEAN, INCREMENTAL};
	public static enum TestSubject {HOTSPOTS, MARKERS};
	private static final Pattern filePattern = Pattern.compile("^(.*\\.java)|(\\.alvor)$", Pattern.CASE_INSENSITIVE);
	private final IProject project;
	private final IPath resultsFolder;
	private final TestScenario testScenario;
	private final TestSubject testSubject;
	
	public static void runOn(String projectName, TestScenario testScenario, TestSubject testSubject) {
		runOn(WorkspaceUtil.getProject(projectName), testScenario, testSubject);
	}
	
	public static void runOn(IProject project, TestScenario testScenario, TestSubject testSubject) {
		ProjectBasedTester test = new ProjectBasedTester(project, testScenario, testSubject);
		test.testAlvorFeaturesAsRequiredByProject();
	}
	
    private ProjectBasedTester(IProject project, TestScenario testScenario, TestSubject testsubject) {
		this.project = project;
		this.testScenario = testScenario;
		this.testSubject = testsubject;
		this.resultsFolder = TestUtil.getTestResultsFolder(project, null);
	}
    
    public void testAlvorFeaturesAsRequiredByProject() {
    	try {
	    	waitForBuild();
	    	// initialize project
			if (this.testScenario == TestScenario.INCREMENTAL) {
				MarkedFileChanger.undoAllChangesInProject(project, filePattern);
		    	waitForBuild();
			}
			
			// always test hotspot strings
//			CacheProvider.getCache().clearProject(project.getName());
//			StringCollector.updateProjectCache(project, CacheProvider.getCache(), null);
			performCleanBuild();
	    	waitForBuild();
	    	
	    	if (this.testSubject == TestSubject.HOTSPOTS) {
	    		findAndStoreHotspots(this.resultsFolder);
	    	}
	    	
	    	if (this.testSubject == TestSubject.MARKERS) {
	    		findAndStoreAlvorMarkers(this.resultsFolder);
	    	}
	    	
	    	if (this.testScenario == TestScenario.INCREMENTAL) {
	    		performChangesAndStoreResults();
	    	}
	    	
	    	// now finally validate stuff (when all data is collected)
	    	String differences = TestUtil.findDifferencesInResults(this.resultsFolder.toFile());
	    	if (differences != null && !differences.isEmpty()) {
	    		throw new AssertionError("Diff: " + differences);
	    	}
    	}
	    finally {
			if (this.testScenario == TestScenario.INCREMENTAL) {
				MarkedFileChanger.undoAllChangesInProject(project, filePattern);
		    	waitForBuild();
			}
	    }
    }
    
	private void performCleanBuild() {
		Job job = new Job("clean build") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				GuiChecker.INSTANCE.cleanUpdateProjectMarkers(project, monitor);
				return Status.OK_STATUS;
			}
			
			@Override
			public boolean belongsTo(Object family) {
				return family.equals(ResourcesPlugin.FAMILY_AUTO_BUILD);
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setUser(false);
		job.schedule();
	}

	private void performChangesAndStoreResults() {
		try {
			assert project.hasNature(AlvorNature.NATURE_ID);

			int changeNo = 1;
			while (MarkedFileChanger.applyChangesInProject(project, filePattern, changeNo)) {
				waitForBuild();
				IPath folder = TestUtil.getTestResultsFolder(project, "change_" + changeNo);
		    	if (this.testSubject == TestSubject.HOTSPOTS) {
		    		findAndStoreHotspots(folder);
		    	}
		    	if (this.testSubject == TestSubject.MARKERS) {
		    		findAndStoreAlvorMarkers(folder);
		    	}
				changeNo++;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void findAndStoreAlvorMarkers(IPath folder) {
		List<String> hotspotMarkers = WorkspaceUtil.getMarkersAsStrings(this.project, AlvorGuiPlugin.HOTSPOT_MARKER_ID);
		hotspotMarkers.addAll(WorkspaceUtil.getMarkersAsStrings(this.project, AlvorGuiPlugin.UNSUPPORTED_MARKER_ID));
		findAndStoreMarkers(hotspotMarkers, AlvorGuiPlugin.HOTSPOT_MARKER_ID, folder);
		
		findAndStoreMarkers(null, AlvorGuiPlugin.ERROR_MARKER_ID, folder);
		findAndStoreMarkers(null, AlvorGuiPlugin.WARNING_MARKER_ID, folder);
	}
	
	private void findAndStoreHotspots(IPath folder) {
		List<HotspotDescriptor> hotspots = (CacheProvider.getCache(this.project.getName()).getPrimaryHotspots(false));
		TestUtil.storeFoundHotspotInfo(hotspots, folder);
	}
    
    private void findAndStoreMarkers(List<String> markers, String markerId, IPath folder) {
    	if (markers == null) {
    		markers = WorkspaceUtil.getMarkersAsStrings(this.project, markerId);
    	}
		String shortId = markerId.substring(markerId.lastIndexOf('.')+1);
		Collections.sort(markers);
		TestUtil.storeFoundTestResults(markers, folder, shortId);
    }
    
    private void waitForBuild() {
    	try {
			Thread.sleep(2000); // FIXME should be more intelligent
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    	// got it from here:
    	// http://www.devdaily.com/java/jwarehouse/eclipse/org.eclipse.core.tests.resources/src/org/eclipse/core/tests/resources/ResourceTest.java.shtml
		try {
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
		} catch (OperationCanceledException e) {
			//ignore
		} catch (InterruptedException e) {
			//ignore
		}
	}    
}
