package com.zeroturnaround.alvor.crawler;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.junit.Test;

import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.configuration.ConfigurationManager;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;

public class MyCacheBuilderTest {
	
	@Test
	public void cleanBuildProjectCache() {
		try {
			String projectName = "earved";
			
			IJavaProject javaProject = JavaModelUtil.getJavaProjectByName(projectName);
			JavaModelUtil.openProject(javaProject);
			ProjectConfiguration conf = ConfigurationManager
				.readProjectConfiguration(javaProject.getProject(), true);
			CacheProvider.getCache().setProjectPrimaryPatterns(javaProject.getProject().getName(), conf.getHotspotPatterns());
			
			
//			FullParseCacheBuilder cb = new FullParseCacheBuilder();
			SearchBasedCacheBuilder cb = new SearchBasedCacheBuilder();
			cb.fullBuildProject(javaProject.getProject(), null);
			
			
			Timer backTimer = new Timer("Back");
			CacheProvider.getCache().printQueryCount();
			List<NodeDescriptor> result = CacheProvider.getCache().getProjectHotspots(projectName);
			for (NodeDescriptor desc : result) {
				System.out.println("RES: " + desc);
			}
			backTimer.printTime();
			CacheProvider.getCache().printQueryCount();
		}
		finally {
			CacheProvider.shutdownCache();
		}
		
//		// check that cache contains correct strings
//		Cache cache = null;
//		Collection<NodeDescriptor> strings = cache.getPrimaryHotspotDescriptors
//			(javaProject.getProject().getName());
//		
//		List<String> serializedStrings = new ArrayList<String>();
//		// TODO serialize strings
//		assertTrue(CrawlerTestUtil.stringsAreExpected(serializedStrings, "earved_strings_cache"));
	}
	
//	public void updateCacheAfterOneFileChange() {
//		ICompilationUnit unit = JavaModelUtil.getCompilationUnitByName("/earved/src/.../blaa.java");
//		CacheBuilder.updateCacheAfterChange(unit.getJavaProject(), Collections.singletonList(unit));
//		Cache cache = null;
//		
//		Collection<NodeDescriptor> descriptors = cache.getPrimaryHotspotDescriptors(unit.getElementName());
//		
//		// TODO compare with expected
//	}
}
