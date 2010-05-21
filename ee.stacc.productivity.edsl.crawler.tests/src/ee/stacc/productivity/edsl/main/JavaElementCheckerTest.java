package ee.stacc.productivity.edsl.main;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.crawler.PositionUtil;
import ee.stacc.productivity.edsl.crawler.UnsupportedNodeDescriptor;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.samplegen.SampleGenerator;

public class JavaElementCheckerTest {
	JavaElementChecker checker = new JavaElementChecker();
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	private static final String TEST_FOLDER = 
		"C:/edslws/ee.stacc.productivity.edsl.crawler.tests/tests";
	
	public JavaElementCheckerTest()  {
		
	}
	
//	@Test
//	public void testEDSLTestProjectStrings() throws IOException, CoreException {
//		testJavaElementAbstractStrings("EDSLTestProject", "");
//	}
	
	@Test
	public void testEArvedStrings() throws IOException, CoreException {
		testJavaElementAbstractStrings("earved", "src");
	}
	
//	@Test
//	public void testSqlBusStrings() throws IOException, CoreException {
//		testJavaElementAbstractStrings("sql_bus", "");
//	}
	
	
	private void testJavaElementAbstractStrings(String projectName, 
			String packageFragmentRoot) throws IOException, CoreException {
		
		// clear cache
		CacheService.getCacheService().clearAll();
		CacheService.getCacheService().setNocache(true);
		
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
		File abstractOutputFile = new File(filePrefix + "_found.txt");
		if (abstractOutputFile.exists()) {
			abstractOutputFile.delete();
		}
		
		File concreteOutputFile = new File(filePrefix + "_found_concrete.txt");
		if (concreteOutputFile.exists()) {
			concreteOutputFile.delete();
		}
		
		
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
		
		//Collections.sort(lines);
		PrintStream abstractOutput = new PrintStream(abstractOutputFile);
		for (String s : abstractLines) {
			abstractOutput.println(s);
		}
		
		Collections.sort(concreteLines);
		PrintStream concreteOutput = new PrintStream(concreteOutputFile);
		for (String s : concreteLines) {
			concreteOutput.println(s);
		}
		
		

		File expectedConcreteFile = new File(filePrefix + "_expected_concrete.txt");
		assertTrue("Expected concrete strings != found concrete strings: " + id, 
					filesAreEqual(concreteOutputFile, expectedConcreteFile));
		
		File expectedAbstractFile = new File(filePrefix + "_expected.txt");
		assertTrue("Expected abstract strings != found abstract strings: " + id, 
					filesAreEqual(abstractOutputFile, expectedAbstractFile));

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
