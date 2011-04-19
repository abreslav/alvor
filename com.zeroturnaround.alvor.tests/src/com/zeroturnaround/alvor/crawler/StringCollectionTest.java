package com.zeroturnaround.alvor.crawler;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.junit.Test;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.WorkspaceUtil;
import com.zeroturnaround.alvor.tests.util.TestUtil;

/**
 * Tests Cache, StringCollector and Crawler
 * @author Aivar
 *
 */
public abstract class StringCollectionTest {
	protected abstract String getProjectName();
	
	@Test
	public void findValidNodeDescriptors() {
		String projectName = this.getProjectName();
		try {
			CacheProvider.getCache(projectName).clearProject();
			findAndValidateNodeDescriptors(projectName);
		}
		finally {
			CacheProvider.getCache(projectName).printDBInfo();
			CacheProvider.shutdownCaches();
		}
	}
	
	private void findAndValidateNodeDescriptors(String projectName) {
		IProject project = WorkspaceUtil.getProject(projectName);
		IPath resultsFolder = TestUtil.getTestResultsFolder(project, null);
		TestUtil.storeFoundHotspotInfo(getNodeDescriptors(project), resultsFolder);
		validateFoundHotspotInfo(resultsFolder);
	}
	
	private List<HotspotDescriptor> getNodeDescriptors(IProject project) {
		Cache cache = CacheProvider.getCache(project.getName());
		StringCollector.updateProjectCache(project, null);
		return cache.getPrimaryHotspots(true);
	}

	private void validateFoundHotspotInfo(IPath folder) {
		boolean concreteResult = foundFilesAreExpected(folder, "concrete_strings");
		boolean sortedAbstractResult = foundFilesAreExpected(folder, "node_descriptors_sorted");
		boolean abstractResult = foundFilesAreExpected(folder, "node_descriptors");
		boolean positionResult = foundFilesAreExpected(folder, "node_positions");
		
		if (!positionResult) {
			throw new AssertionError("Positions are different");
		}
		else if (!concreteResult) {
			throw new AssertionError("Concretes are different");
		}
		else if (!sortedAbstractResult) {
			throw new AssertionError("Abstract are different, but concretes are same");
		}
		else if (!abstractResult) {
			throw new AssertionError("Abstract result is in different order");
		}
		else {
			// all OK
		}
	}

	private boolean foundFilesAreExpected(IPath folder, String topic) {
		return TestUtil.filesAreEqual(folder.append(topic + "_found.txt").toFile(), 
				folder.append(topic + "_expected.txt").toFile());
	}
	
}
