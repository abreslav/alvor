package ee.stacc.productivity.edsl.main;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;

import ee.stacc.productivity.edsl.checkers.INodeDescriptor;

public class JavaElementCheckerTest {
	JavaElementChecker checker = new JavaElementChecker();
	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	
	public JavaElementCheckerTest()  {
		
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
		IJavaProject project = (IJavaProject)root.getProject(projectName).getNature(JavaCore.NATURE_ID);
		//out.println(project);
		if (packageFragmentRoot.isEmpty()) {
			testJavaElementAbstractStrings(project);
		}
		else {
			testJavaElementAbstractStrings(project.findPackageFragmentRoot
					(project.getPath().append(packageFragmentRoot)));
		}
	}
	
	private void testJavaElementAbstractStrings(IJavaElement element) throws IOException {
		Map<String, Object> options = OptionLoader.getElementSqlCheckerProperties(element);
		List<INodeDescriptor> hotspots = checker.findHotspots(element, options);
		
		// temporary
		assertEquals(359, hotspots.size());
		// TODO serialize hotspots to a file
		// and compare with expected file
	}
}
