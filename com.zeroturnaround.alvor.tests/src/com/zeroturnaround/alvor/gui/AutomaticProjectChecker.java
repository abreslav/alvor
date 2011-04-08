package com.zeroturnaround.alvor.gui;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;

import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.crawler.CrawlerTestUtil;
import com.zeroturnaround.alvor.crawler.StringCollector;
import com.zeroturnaround.alvor.gui.changetests.MarkedFileChanger;

/*
 * Meant for checking "alvor-test-projects"
 * Is used by ScriptedProjectTest
 * 
 * Checks project according to its name
 * Stores test results in "AlvorTestResults" folder and compares with
 */
public class AutomaticProjectChecker {
	private static Pattern fileNamePattern = Pattern.compile("^(.*\\.java)|(\\.alvor)$", Pattern.CASE_INSENSITIVE);
	
	public void checkProject(IProject project) {
		
		if (project.getName().contains("_changes")) {
			MarkedFileChanger.undoAllChangesInProject(project, fileNamePattern);
		}
		
		GuiChecker.INSTANCE.cleanUpdateProjectMarkers(project, null);
		
		// Get strings (ie. testing string collector and cache)
		CacheProvider.getCache().clearProject(project.getName());
		StringCollector.updateProjectCache(project, CacheProvider.getCache(), null);
		List<HotspotDescriptor> hotspots = (CacheProvider.getCache().getPrimaryHotspots(project.getName()));
		CrawlerTestUtil.validateNodeDescriptors(hotspots, project);
		
		if (project.getName().contains("_markers")) {
			
		}
		
	}
	
	private boolean shouldTestChanges(IProject project) {
		return project.getName().startsWith("Change_");
	}
	
	private void cleanProject(IProject project) {
		
	}
}
