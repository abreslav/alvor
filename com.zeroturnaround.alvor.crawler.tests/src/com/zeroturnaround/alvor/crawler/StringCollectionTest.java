package com.zeroturnaround.alvor.crawler;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.junit.Test;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.WorkspaceUtil;

/**
 * Tests Cache, StringCollector and Crawler
 * @author Aivar
 *
 */
public abstract class StringCollectionTest {
	@Test
	public void findValidNodeDescriptors() {
		try {
			String projectName = this.getProjectName();
			// CacheProvider.getCache().clearProject(projectName);
			CacheProvider.getCache().clearAllProjects();
			findAndValidateNodeDescriptors(projectName);
		}
		finally {
			CacheProvider.getCache().printDBInfo();
			CacheProvider.shutdownCache();
		}
	}
	
	protected abstract String getProjectName();
	
	protected void findAndValidateNodeDescriptors(String projectName) {
		IProject project = WorkspaceUtil.getProject(projectName);
		CrawlerTestUtil.validateNodeDescriptors(getNodeDescriptors(project), project);
	}
	
	private List<HotspotDescriptor> getNodeDescriptors(IProject project) {
		Cache cache = CacheProvider.getCache();
		StringCollector.updateProjectCache(project, cache, null);
		return cache.getUncheckedPrimaryHotspots(project.getName());
	}
	
}
