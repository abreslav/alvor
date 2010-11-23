package com.zeroturnaround.alvor.gui;
/*
 * - clean cache
 * - compute everything
 * - remember abstract strings (and compares with confirmed results)
 * - make dummy changes to some files (eg. adds spaces to end)
 * - let builder update cache
 * 		- count number of abstract strings invalidated
 * 		- compare cache content (number of rows) with initial content
 * - re-collect abstract-strings using cache
 * 		- no crawling should be necessary now
 * 		- result should be equal to initial
 * - make some real changes (eg. delete or add a call to a method containing 
 * 		hotspot(when part of expression comes from argument))  	
 * - restore changed files to original content
 * 		 
 */

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Assert;

import com.zeroturnaround.alvor.cache.PositionUtil;
import com.zeroturnaround.alvor.checkers.INodeDescriptor;
import com.zeroturnaround.alvor.checkers.IStringNodeDescriptor;
import com.zeroturnaround.alvor.crawler.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.samplegen.SampleGenerator;

public abstract class GUITest {
	GuiChecker checker = new GuiChecker();
	
//	protected void setProject(String projectName) {
//		this.project = root.getProject(projectName);
//		
//		try {
//			if (!project.isOpen()) {
//				project.open(null);
//			}
//			
//			this.javaProject = (IJavaProject)project.getNature(JavaCore.NATURE_ID);
//		} catch (CoreException e) {
//			throw new IllegalStateException(e);
//		}
//	}
	
	public GUITest() {
	}
	
	protected static IProject getProject(String projectName) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (!project.isOpen()) {
			project.open(null);
		}
		return project;
	}
	
	protected static IJavaProject getJavaProject(String projectName) throws CoreException {
		return (IJavaProject)GUITest.getProject(projectName).getNature(JavaCore.NATURE_ID);
	}
	
	protected String testAbstractStringsClean(IJavaElement element) throws FileNotFoundException {
		List<INodeDescriptor> hotspots = checker.performCleanCheck(element, new IJavaElement[] {element});
		return writeAndTestHotspots(hotspots, getElementDescriptor(element));
	}
	
	/*
	 * Make dummy change in a file and check that resulting markers are same
	 */
	protected void makeDummyChange(IProject project, String filename) throws CoreException {
		IResource res = project.findMember(filename); 
		res.touch(null);
	}
	
	protected String writeAndTestMarkers(IJavaElement element, String markerType, String testTitle,
			boolean includeLocationInfo) throws FileNotFoundException {
		try {
			List<String> lines = new ArrayList<String>();
			IMarker[] errors = element.getResource().findMarkers(markerType, false, IResource.DEPTH_INFINITE);
			for (IMarker err: errors) {
				String line = err.getAttribute(IMarker.MESSAGE, "<no message>");
				if (includeLocationInfo) {
					int lineNum = PositionUtil.getLineNumber(
							(IFile)err.getResource(), 
							err.getAttribute(IMarker.CHAR_START, 0));
					line = line + ", at: " 
							+ err.getAttribute(IMarker.LOCATION, "<no location>")
							+ ":" + lineNum;
				}
				lines.add(line);
			}
			Collections.sort(lines);
			return writeAndCompare(lines, getElementDescriptor(element) + "_" + testTitle);
		} catch (CoreException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private String writeAndTestHotspots(List<INodeDescriptor> hotspots, String elementId) throws FileNotFoundException {
		List<String> abstractLines = new ArrayList<String>();
		List<String> concreteLines = new ArrayList<String>();
		
		for (INodeDescriptor desc : hotspots) {
			String start = PositionUtil.getLineString(desc.getPosition()) + ", ";
			
			if (desc instanceof IStringNodeDescriptor) {
				IAbstractString aStr = ((IStringNodeDescriptor)desc).getAbstractValue(); 
				abstractLines.add(start + aStr.toString());
				try {
					concreteLines.addAll(SampleGenerator.getConcreteStrings(aStr));
				} catch (Exception e) {
					concreteLines.add("ERROR GENERATING SAMPLES: " + e.getMessage()
							+ ", POS=" + aStr.getPosition() + ", ABS_STR=" + aStr);
				}
			}
			else if (desc instanceof UnsupportedNodeDescriptor) {
				abstractLines.add(start + "unsupported: " 
						+ ((UnsupportedNodeDescriptor)desc).getProblemMessage());
			}
			else {
				abstractLines.add("???");
			}
		}
		
		Collections.sort(concreteLines);
		return writeAndCompare(concreteLines, elementId + "_concrete") + 
			writeAndCompare(abstractLines, elementId + "_abstract");
	}
	
	public static IJavaElement getSourceFolder(IJavaProject project, String folderName) {
		try {
			return project.findPackageFragmentRoot(project.getPath().append(folderName));
		} catch (JavaModelException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	private String getElementDescriptor(IJavaElement e) {
		if (e instanceof IJavaProject) {
			return e.getJavaProject().getElementName().replace('/', '_');
		}
		else {
			return (e.getJavaProject().getElementName() + "_" + e.getElementName()).replace('/', '_');
		}
	}
	
	private String writeAndCompare(List<String> items, String testId) throws FileNotFoundException {
		
		String filePrefix = "results/" + getWorkspaceName() + "_" + testId;
		File outFile = new File(filePrefix + "_found.txt");
		if (outFile.exists()) {
			outFile.delete();
		}
		PrintStream outStream = new PrintStream(outFile);
		for (String item : items) {
			outStream.println(item);
		}
		outStream.close();
		
		File expectedFile = new File(filePrefix + "_expected.txt");
		if (!filesAreEqual(outFile, expectedFile)) {
			return filePrefix + ": found strings != expected strings; ";
		} 
		else {
			return "";
		}
	}
	
	boolean filesAreEqual(File a, File b) throws FileNotFoundException {
		assert a.exists() && b.exists();
		
		Scanner scA = new Scanner(a);
		Scanner scB = new Scanner(b);
		
		while (scA.hasNextLine()) {
			if (!scB.hasNextLine() || ! scA.nextLine().equals(scB.nextLine())) {
				return false;
			}
		}
		
		return true;
	}
	
	public String getWorkspaceName() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getName();
	}
	
	void normalTest(IJavaElement element) throws FileNotFoundException {
		String err = testAbstractStringsClean(element)
			+ writeAndTestMarkers(element, GuiChecker.ERROR_MARKER_ID, "errors", true)
			+ writeAndTestMarkers(element, GuiChecker.WARNING_MARKER_ID, "warnings", true);
		if (!err.isEmpty()) {
			Assert.fail(err);
		}
	}
}
