package com.zeroturnaround.alvor.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import com.zeroturnaround.alvor.tests.util.LabelledParameterized;
import com.zeroturnaround.alvor.tests.util.ProjectBasedTester;


@RunWith(value=LabelledParameterized.class)
public class MultiProjectTest {
	private final IProject project;
	private final boolean testChanges;
	private final boolean testMarkers;
	
	public MultiProjectTest(IProject project, boolean testChanges, boolean testMarkers) {
		this.project = project;
		this.testChanges = testChanges;
		this.testMarkers = testMarkers;
	}
	
	@Test
	public void testOnProject() {
		ProjectBasedTester.runOn(this.project, testChanges, testMarkers);
	}

	/**
	 * returns the projects for constructing actual test instances
	 */
	@Parameters
    public static List<Object[]> getParameters() {
    	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    	List<Object[]> parameters = new ArrayList<Object[]>();
    	
    	for (IProject project : projects) {
    		Boolean testChanges = project.getName().contains("_changes");
    		Boolean testMarkers = project.getName().contains("_markers");
    		parameters.add(new Object[]{project, testChanges, testMarkers});
    	}
        return parameters;
    }
    
}
