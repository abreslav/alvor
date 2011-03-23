package com.zeroturnaround.alvor.gui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.WorkspaceUtil;

public class GuiFacade {
	public static void executeAlvorCleanCheck(IProject project) throws Exception {
		// TODO should do it via GUI only
		GuiChecker checker = new GuiChecker();
		IJavaProject javaProject = (IJavaProject)project.getNature(JavaCore.NATURE_ID);
		IJavaElement[] scope = {javaProject};
		
		checker.performCleanCheck(project, scope, null);
	}
	
	public static void startIncrementalBuild(IProject project) {
		try {
			ICommand[] builders = project.getDescription().getBuildSpec();
			System.out.println(builders);
		} catch (CoreException e1) {
			throw new RuntimeException(e1);
		}
		
		try {
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Deprecated
	public static void executeClean() {
		
	}
	
	@Deprecated
	public static void selectAnItemInPackageExplorer(String filename) {
		
	}
	
	public static void trimFile(String filename) throws IOException, CoreException {
		String content = getFileContent(filename);
		setFileContent(filename, content.trim());
	}
	
	public static void prependEmptyLine(String filename) throws IOException, CoreException {
		String content = getFileContent(filename);
		setFileContent(filename, "\n" + content.trim());
	}
	
	public static void appendEmptyLine(String filename) throws IOException, CoreException {
		String content = getFileContent(filename);
		setFileContent(filename, content.trim() + "\n");
	}
	
	private static String getFileContent(String filename) throws IOException, CoreException {
		return getFileContent(WorkspaceUtil.getFile(filename));
	}
	
	private static String getFileContent(IFile file) throws IOException, CoreException {
		InputStream stream = file.getContents();
		String result = convertStreamToString(stream);
		stream.close();
		return result;
	}
	
	public static void setFileContent(String filename, String text) throws CoreException {
		setFileContent(WorkspaceUtil.getFile(filename), text);
	}
	
	public static void setFileContent(IFile file, String text) throws CoreException {
		file.setContents(new ByteArrayInputStream(text.getBytes()), 0, null);
	}
	
	public static void touchFile(String filename) throws CoreException {
		IFile file = WorkspaceUtil.getFile(filename);
		file.touch(null);
	}
	
	public static void waitUntilAlvorHasCompleted() {
		// TODO check job queue or smth like that
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
//	
//	public static void renameAClass(String filename) {
//		
//	}
//	
//	public static void renameAMethod(String filename) {
//		
//	}
	
	public static List<String> getMarkersAsStrings(IProject project, String markerId) throws CoreException {
		assert project != null;
		
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
	
	public static boolean projectHasJavaErrors(IJavaProject project) throws JavaModelException, CoreException {
		return containsMarkers(project.getUnderlyingResource(), IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
	}
	
	private static boolean containsMarkers(IResource res, String markerId) throws CoreException {
		IMarker[] markers = res.findMarkers(markerId, true, IResource.DEPTH_INFINITE);
		
		for (IMarker marker : markers) {
			if (marker.getType().equals(markerId)) {
				return true;
			}
		}
		
		return false;
	}

	private static String convertStreamToString(InputStream is) throws IOException {
	    /* http://www.kodejava.org/examples/266.html
	     * To convert the InputStream to String we use the
	     * Reader.read(char[] buffer) method. We iterate until the
	     * Reader return -1 which means there's no more data to
	     * read. We use the StringWriter class to produce the string.
	     */
	    if (is != null) {
	        Writer writer = new StringWriter();
	
	        char[] buffer = new char[1024];
	        try {
	            Reader reader = new BufferedReader(
	                    new InputStreamReader(is, "UTF-8"));
	            int n;
	            while ((n = reader.read(buffer)) != -1) {
	                writer.write(buffer, 0, n);
	            }
	        } finally {
	            is.close();
	        }
	        return writer.toString();
	    } else {       
	        return "";
	    }
	}
}
