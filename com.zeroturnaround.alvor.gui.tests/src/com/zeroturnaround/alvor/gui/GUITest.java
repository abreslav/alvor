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
import org.junit.Test;

import com.zeroturnaround.alvor.checkers.INodeDescriptor;
import com.zeroturnaround.alvor.checkers.IStringNodeDescriptor;
import com.zeroturnaround.alvor.crawler.PositionUtil;
import com.zeroturnaround.alvor.crawler.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.samplegen.SampleGenerator;

public class GUITest {
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	GuiChecker checker = new GuiChecker();
	
	@Test
	public void testEArved() throws JavaModelException, FileNotFoundException {
		performComplexTestOnJavaElement(getJavaElement("earved", "src")); 
	}
	
	private void performComplexTestOnJavaElement(IJavaElement element) throws FileNotFoundException {
		List<INodeDescriptor> hotspots = checker.performCheck(element, new IJavaElement[] {element});
		writeAndTestHotspots(hotspots, getElementDescriptor(element));
		writeAndTestMarkers(element);
	}
	
	private void writeAndTestMarkers(IJavaElement element) throws FileNotFoundException {
		String testId = getElementDescriptor(element);
		try {
			List<String> lines = new ArrayList<String>();
			
			IMarker[] errors = element.getResource().findMarkers(GuiChecker.ERROR_MARKER_ID, false, IResource.DEPTH_INFINITE);
			lines.clear();
			for (IMarker err: errors) {
				lines.add(err.getAttribute(IMarker.MESSAGE, "<no message>"));
			}
			Collections.sort(lines);
			writeAndCompare(lines, testId, "errors");
			
			IMarker[] warnings = element.getResource().findMarkers(GuiChecker.WARNING_MARKER_ID, false, IResource.DEPTH_INFINITE);
			lines.clear();
			for (IMarker war: warnings) {
				lines.add(war.getAttribute(IMarker.MESSAGE, "<no message>"));
			}
			Collections.sort(lines);
			writeAndCompare(lines, testId, "warnings");
			
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void writeAndTestHotspots(List<INodeDescriptor> hotspots, String testId) throws FileNotFoundException {
		List<String> abstractLines = new ArrayList<String>();
		List<String> concreteLines = new ArrayList<String>();
		
		for (INodeDescriptor desc : hotspots) {
			String start = PositionUtil.getLineString(desc.getPosition()) + ", ";
			
			if (desc instanceof IStringNodeDescriptor) {
				IAbstractString aStr = ((IStringNodeDescriptor)desc).getAbstractValue(); 
				abstractLines.add(start + aStr.toString());
				concreteLines.addAll(SampleGenerator.getConcreteStrings(aStr));
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
		writeAndCompare(concreteLines, testId, "concrete");
		writeAndCompare(abstractLines, testId, "abstract");
	}
	
	protected IJavaElement getJavaElement(String projectName, String packageFragmentRoot) throws JavaModelException {
		IJavaProject project = getJavaProject(projectName);
		if (packageFragmentRoot.isEmpty()) {
			return project;
		}
		else {
			return project.findPackageFragmentRoot(project.getPath().append(packageFragmentRoot));
		}
	}
	
	private IJavaProject getJavaProject(String projectName) {
		IProject project = root.getProject(projectName);
		
		try {
			if (!project.isOpen()) {
				project.open(null);
			}
			
			return (IJavaProject)project.getNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	private String getElementDescriptor(IJavaElement e) {
		return (e.getJavaProject().getElementName() + "_" + e.getElementName()).replace('/', '_');
	}
	
	private void writeAndCompare(List<String> items, String testId, String topic) throws FileNotFoundException {
		
		String filePrefix = "results/" + testId + "_" + topic;
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
		assertTrue(filePrefix + ": found strings != expected strings", 
				filesAreEqual(outFile, expectedFile));
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
}
