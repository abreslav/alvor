package com.zeroturnaround.alvor.gui;

import java.io.FileNotFoundException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Test;

public class GuiTestEArved extends GUITest {
	
	@Test
	public void testStringsAndMarkers() throws FileNotFoundException, CoreException {
		IJavaProject javaProject = GUITest.getJavaProject("earved"); 
		IJavaElement element = GUITest.getSourceFolder(javaProject, "src");
		this.normalTest(element);
	}

//	@Test
//	public void testChange() throws FileNotFoundException, CoreException {
//		makeDummyChange("src/ee/post/earved/dao/client/ClientRegisterDAO.java");
//		// TODO get some info about number of searches or smth. like this
//		// TODO wait until analysis is finished
//		writeAndTestMarkers(element, GuiChecker.ERROR_MARKER_ID, "errors", true);
//		writeAndTestMarkers(element, GuiChecker.WARNING_MARKER_ID, "warnings", true);
//	}

}
