package ee.stacc.productivity.edsl.main;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;

import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.crawler.PositionUtil;
import ee.stacc.productivity.edsl.crawler.UnsupportedNodeDescriptor;

public class JavaElementCheckerTest {
	JavaElementChecker checker = new JavaElementChecker();
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	private static final String TEST_FOLDER = 
		"C:/edslws/ee.stacc.productivity.edsl.crawler.tests/tests";
	
	public JavaElementCheckerTest()  {
		
	}
	
	@Test
	public void testEDSLTestProjectStrings() throws IOException, CoreException {
		testJavaElementAbstractStrings("EDSLTestProject", "");
	}
	
	@Test
	public void testEArvedStrings() throws IOException, CoreException {
		testJavaElementAbstractStrings("earved", "src");
	}
	
	/*@Test
	public void testCompiereString() throws IOException, CoreException {
		testJavaElementAbstractStrings("compiere", "");
	}
	*/
	
	private void testJavaElementAbstractStrings(String projectName, 
			String packageFragmentRoot) throws IOException, CoreException {
		
		IProject project = root.getProject(projectName);
		
		if (!project.isOpen()) {
			project.open(null);
		}
		
		IJavaProject javaProject = (IJavaProject)project.getNature(JavaCore.NATURE_ID);
		
		String id = projectName + "_" + packageFragmentRoot;
		
		if (packageFragmentRoot.isEmpty()) {
			testJavaElementAbstractStrings(javaProject, id);
		}
		else {
			testJavaElementAbstractStrings(javaProject.findPackageFragmentRoot
					(javaProject.getPath().append(packageFragmentRoot)), id);
		}
	}
	
	private void testJavaElementAbstractStrings(IJavaElement element, String id) throws IOException {
		Map<String, Object> options = OptionLoader.getElementSqlCheckerProperties(element);
		List<INodeDescriptor> hotspots = checker.findHotspots(new IJavaElement[] {element}, options);
		
		String filePrefix = TEST_FOLDER + "/" + id;
		File outputFile = new File(filePrefix + "_found.txt");
		if (outputFile.exists()) {
			outputFile.delete();
		}
		
		List<String> lines = new ArrayList<String>();
		
		for (INodeDescriptor desc : hotspots) {
			String start = PositionUtil.getLineString(desc.getPosition()) + ", ";
			
			if (desc instanceof IStringNodeDescriptor) {
				lines.add(start + 
						((IStringNodeDescriptor)desc).getAbstractValue().toString());
			}
			else if (desc instanceof UnsupportedNodeDescriptor) {
				lines.add(start + "unsupported: " 
						+ ((UnsupportedNodeDescriptor)desc).getProblemMessage());
			}
			else {
				lines.add("???");
			}
		}
		
		//Collections.sort(lines);
		PrintStream output = new PrintStream(outputFile);
		
		for (String s : lines) {
			output.println(s);
		}
		
		File expectedFile = new File(filePrefix + "_expected.txt");
		
		assertTrue("Expected abstract strings != found abstract strings: " + expectedFile.getParent(), 
					filesAreEqual(outputFile, expectedFile));

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
