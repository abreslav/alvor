package com.zeroturnaround.alvor.crawler;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;

import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.WorkspaceUtil;
import com.zeroturnaround.alvor.configuration.ConfigurationManager;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

public abstract class AbstractStringEvaluatorTest {
	
	@Test
	public abstract void findValidNodeDescriptors();
	
	protected void findAndValidateNodeDescriptors(String projectName) {
		try {
			List<HotspotDescriptor> descriptors;
			IProject project = WorkspaceUtil.getProject(projectName);
			descriptors = getNodeDescriptors(project);
			CrawlerTestUtil.validateNodeDescriptors(descriptors, project);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private List<HotspotDescriptor> getNodeDescriptors(IProject project) throws CoreException {
		IJavaElement[] scope = {(IJavaProject)project.getNature(JavaCore.NATURE_ID)}; 
		ProjectConfiguration conf = ConfigurationManager.readProjectConfiguration(project, true);
		return AbstractStringEvaluator.evaluateMethodArgumentAtCallSites
			(conf.getHotspotPatterns(), scope, 0, null, null);
	}
	
}
