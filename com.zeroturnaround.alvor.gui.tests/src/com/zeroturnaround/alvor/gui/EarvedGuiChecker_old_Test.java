package com.zeroturnaround.alvor.gui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.BeforeClass;

import com.zeroturnaround.alvor.common.WorkspaceUtil;

public class EarvedGuiChecker_old_Test extends AbstractMarkerTest {
	@BeforeClass
	public static void createMarkers() throws CoreException {
		IProject project = WorkspaceUtil.getProject("earved");
		GuiChecker checker = new GuiChecker();
		
		IJavaProject javaProject = (IJavaProject)project.getNature(JavaCore.NATURE_ID);
		IJavaElement[] scope = {javaProject};
		
		checker.performCleanCheck(project, scope, null);
		
		AbstractMarkerTest.selectedProject = project;
	}
	

}
