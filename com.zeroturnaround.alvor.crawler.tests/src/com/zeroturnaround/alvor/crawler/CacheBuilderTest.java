package com.zeroturnaround.alvor.crawler;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Test;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.configuration.ConfigurationManager;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;

public class CacheBuilderTest {
	
//	@Test
//	public void cleanBuildProjectCache() {
//		IJavaProject javaProject = JavaModelUtil.getJavaProjectByName("earved");
//		ProjectConfiguration conf = ConfigurationManager
//			.readProjectConfiguration(javaProject.getProject(), true);
//		CacheBuilder.cleanBuildProjectCache(javaProject, conf);
//		
//		// check that cache contains correct strings
//		Cache cache = null;
//		Collection<NodeDescriptor> strings = cache.getPrimaryHotspotDescriptors
//			(javaProject.getProject().getName());
//		
//		List<String> serializedStrings = new ArrayList<String>();
//		// TODO serialize strings
//		assertTrue(CrawlerTestUtil.stringsAreExpected(serializedStrings, "earved_strings_cache"));
//	}
//	
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
