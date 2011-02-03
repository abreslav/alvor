package com.zeroturnaround.alvor.crawler;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.Test;

import com.zeroturnaround.alvor.cache.ICache;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.common.WorkspaceUtil;

public class CacheBuilderTest {
	
	@Test
	public void buildCorrectFullProjectCache() {
		IProject project = WorkspaceUtil.getProject("earved");
		CacheBuilder.clearAll();
		CacheBuilder.updateProject(project);
		
		// check that cache contains correct strings
		ICache cache = null;
		Collection<NodeDescriptor> strings = cache.getPrimaryHotspotDescriptors(project.getName());
		
		List<String> serializedStrings = new ArrayList<String>();
		// TODO serialize strings
		assertTrue(CrawlerTestUtil.stringsAreExpected(serializedStrings, "earved_strings_cache"));
	}
	
	public void updateCacheForOneFile() {
		IFile file = WorkspaceUtil.getFile("/earved/src/.../blaa.java");
		CacheBuilder.updateFile(file);
		ICache cache = null;
		cache.getPrimaryHotspotDescriptors(file.getName());
	}
}
