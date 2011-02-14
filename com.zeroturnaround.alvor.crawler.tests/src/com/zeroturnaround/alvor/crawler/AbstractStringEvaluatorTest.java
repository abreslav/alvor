package com.zeroturnaround.alvor.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;

import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.WorkspaceUtil;
import com.zeroturnaround.alvor.configuration.ConfigurationManager;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.samplegen.SampleGenerator;

public abstract class AbstractStringEvaluatorTest {
	
	@Test
	public abstract void findValidNodeDescriptors();
	
	protected void findAndValidateNodeDescriptors(String projectName) {
		try {
			List<NodeDescriptor> descriptors;
			IProject project = WorkspaceUtil.getProject(projectName);
			descriptors = getNodeDescriptors(project);
			validateNodeDescriptors(descriptors, project);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private List<NodeDescriptor> getNodeDescriptors(IProject project) throws CoreException {
		IJavaElement[] scope = {(IJavaProject)project.getNature(JavaCore.NATURE_ID)}; 
		ProjectConfiguration conf = ConfigurationManager.readProjectConfiguration(project, true);
		return AbstractStringEvaluator.evaluateMethodArgumentAtCallSites
			(conf.getHotspotPatterns(), scope, 0, null);
	}
	
	private void validateNodeDescriptors(List<NodeDescriptor> descriptors, IProject project) {
		List<String> descriptorLines = new ArrayList<String>();
		List<String> concreteLines = new ArrayList<String>();
		
		for (NodeDescriptor desc : descriptors) {
			String start = PositionUtil.getLineString(desc.getPosition()) + ", ";
			
			if (desc instanceof StringNodeDescriptor) {
				IAbstractString aStr = ((StringNodeDescriptor)desc).getAbstractValue(); 
				descriptorLines.add(start + aStr.toString());
				try {
					concreteLines.addAll(SampleGenerator.getConcreteStrings(aStr));
				} catch (Exception e) {
					concreteLines.add("ERROR GENERATING SAMPLES: " + e.getMessage()
							+ ", POS=" + aStr.getPosition() + ", ABS_STR=" + aStr);
				}
			}
			else if (desc instanceof UnsupportedNodeDescriptor) {
				descriptorLines.add(start + "unsupported: " 
						+ ((UnsupportedNodeDescriptor)desc).getProblemMessage());
			}
			else {
				descriptorLines.add("???");
			}
		}
		
		Collections.sort(concreteLines);
		
		File folder = CrawlerTestUtil.getAndPrepareTestResultsFolder(project);
		
		boolean concreteResult = CrawlerTestUtil.stringsAreExpected(concreteLines, 
				folder.getAbsolutePath() + "/concrete_strings");
		boolean abstractResult = CrawlerTestUtil.stringsAreExpected(descriptorLines, 
				folder.getAbsolutePath() + "/node_descriptors");
		
		if (!abstractResult) {
			if (concreteResult) {
				throw new AssertionError("Node descriptors differ from expected, but concretes are same");
			}
			else {
				throw new AssertionError("Node descriptors differ from expected");
			}
		}
	}
	
}
