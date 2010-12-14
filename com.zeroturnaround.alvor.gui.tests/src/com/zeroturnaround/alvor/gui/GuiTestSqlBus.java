package com.zeroturnaround.alvor.gui;

import java.io.FileNotFoundException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Test;

public class GuiTestSqlBus extends GUITest {
	@Test
	public void testStringsAndMarkers() throws FileNotFoundException, CoreException {
		IJavaProject javaProject = GUITest.getJavaProject("sql_bus"); 
		this.normalTest(javaProject);
	}


}
