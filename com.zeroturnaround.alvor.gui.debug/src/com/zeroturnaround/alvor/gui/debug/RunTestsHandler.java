package com.zeroturnaround.alvor.gui.debug;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;
import com.zeroturnaround.alvor.gui.GuiUtil;
import com.zeroturnaround.alvor.tests.util.ProjectBasedTester;


public class RunTestsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IJavaElement element = GuiUtil.getSingleSelectedJavaElement();
			final IProject project;
			
			if (element == null) {
				project = GuiUtil.getCurrentJavaProject().getProject();
			}
			else {
				project = element.getJavaProject().getProject();
			}
			
			if (JavaModelUtil.projectHasJavaErrors(project)) {
				GuiUtil.showDialog("Please correct Java errors before checking SQL", "Problem");
				return null;
			}
			
			// Do it
    		ProjectBasedTester.TestScenario scenario = ProjectBasedTester.TestScenario.CLEAN;
    		ProjectBasedTester.TestSubject subject = ProjectBasedTester.TestSubject.HOTSPOTS;
    		if (project.getName().contains("_incremental")) {
    			scenario = ProjectBasedTester.TestScenario.INCREMENTAL;
    		}
    		if (project.getName().contains("_markers")) {
    			subject = ProjectBasedTester.TestSubject.MARKERS;
    		}
    		try {
    			ProjectBasedTester.runOn(project, scenario, subject);
    			GuiUtil.showDialog("Test completed without errors", "Info");
    		} catch (Throwable e) {
    			GuiUtil.showDialog("Message from test: " + e.getMessage(), "Problem");
    		}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}
