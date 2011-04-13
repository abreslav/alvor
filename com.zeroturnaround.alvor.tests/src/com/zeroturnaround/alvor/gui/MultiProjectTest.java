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
import com.zeroturnaround.alvor.tests.util.ProjectBasedTester.TestScenario;
import com.zeroturnaround.alvor.tests.util.ProjectBasedTester.TestSubject;


@RunWith(value=LabelledParameterized.class)
public class MultiProjectTest {
	private final IProject project;
	private final TestScenario testScenario;
	private final TestSubject testSubject;
	
	public MultiProjectTest(IProject project, TestScenario testScenario, TestSubject testSubject) {
		this.project = project;
		this.testScenario = testScenario;
		this.testSubject = testSubject;
	}
	
	@Test
	public void testOnProject() {
		ProjectBasedTester.runOn(this.project, testScenario, testSubject);
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
