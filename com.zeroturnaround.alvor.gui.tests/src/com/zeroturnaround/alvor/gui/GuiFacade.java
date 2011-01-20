package com.zeroturnaround.alvor.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.zeroturnaround.alvor.common.PositionUtil;

public class GuiFacade {
	public static void executeAlvorCleanCheck(IProject project) throws Exception {
		// TODO should do it via GUI only
		GuiChecker checker = new GuiChecker();
		IJavaElement[] scope = {(IJavaProject)project.getNature(JavaCore.NATURE_ID)};
		checker.performCleanCheck(project, scope);
	}
	
	@Deprecated
	public static void executeClean() {
		
	}
	
	@Deprecated
	public static void selectAnItemInPackageExplorer(String filename) {
		
	}
	
	@Deprecated
	public static void makeDummyChangeInAFile(String filename) {
		
	}
	
	@Deprecated
	public static void waitUntilAlvorHasCompleted() {
		
	}
	
	@Deprecated
	public static void renameAClass(String filename) {
		
	}
	
	@Deprecated
	public static void renameAMethod(String filename) {
		
	}
	
	public static List<String> getMarkersAsStrings(IProject project, String markerId) throws CoreException {
		List<String> lines = new ArrayList<String>();
		IMarker[] markers = project.findMarkers(markerId, false, IResource.DEPTH_INFINITE);
		for (IMarker marker: markers) {
			String line = marker.getAttribute(IMarker.MESSAGE, "<no message>");
			int lineNum = PositionUtil.getLineNumber(
					(IFile)marker.getResource(), 
					marker.getAttribute(IMarker.CHAR_START, 0));
			line = line + ", at: " 
			+ marker.getAttribute(IMarker.LOCATION, "<no location>")
			+ ":" + lineNum;
			lines.add(line);
		}
		return lines;
	}

}
