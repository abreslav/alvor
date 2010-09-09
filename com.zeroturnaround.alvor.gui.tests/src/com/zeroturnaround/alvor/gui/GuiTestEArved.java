package com.zeroturnaround.alvor.gui;

import java.io.FileNotFoundException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.junit.Test;

public class GuiTestEArved extends GUITest {
	IJavaElement element = null;
	
	public GuiTestEArved() {
		setProject("earved");
		element = GUITest.getSourceFolder(this.javaProject, "src");
	}
	
	@Test
	public void testStringsAndMarkers() throws FileNotFoundException {
		testAbstractStringsClean(element);
		writeAndTestMarkers(element, GuiChecker.ERROR_MARKER_ID, "errors", true);
		writeAndTestMarkers(element, GuiChecker.WARNING_MARKER_ID, "warnings", true);
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
