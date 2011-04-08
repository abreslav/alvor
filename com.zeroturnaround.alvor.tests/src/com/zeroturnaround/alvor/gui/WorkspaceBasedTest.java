package com.zeroturnaround.alvor.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import com.zeroturnaround.alvor.tests.util.LabelledParameterized;


@RunWith(value=LabelledParameterized.class)
public class WorkspaceBasedTest {
	private IProject project;
	
	public WorkspaceBasedTest(IProject project) {
		this.project = project;
	}

	/**
	 * returns the projects for constructing actual test instances
	 */
	@Parameters
    public static List<Object[]> getParameters() {
    	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    	List<Object[]> parameters = new ArrayList<Object[]>();
    	
    	for (IProject project : projects) {
    		parameters.add(new Object[]{project});
    	}
        return parameters;
    }
    
}
