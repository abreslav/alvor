package com.zeroturnaround.alvor.gui;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.lkl.common.util.testing.LabelledParameterized;

@RunWith(value=LabelledParameterized.class)
public class ScriptedProjectTest {
	IProject project;
	
    public ScriptedProjectTest(IProject project) {
		this.project = project;
	}
    
	/**
	 * @return the projects for constructing actual test instances
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
    
    @Test
    public void testSmth() {
    	Assert.assertTrue(project.getName(), true);
    }
}
