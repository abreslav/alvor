package com.zeroturnaround.alvor.gui;

import java.io.FileNotFoundException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Test;

public class GuiTestArk extends GUITest {
	
	@Test
	public void testStringsAndMarkers() throws FileNotFoundException, CoreException {
		IJavaProject javaProject = GUITest.getJavaProject("ark"); 
		IJavaElement element = GUITest.getSourceFolder(javaProject, "src");
		this.normalTest(element);
	}

}
