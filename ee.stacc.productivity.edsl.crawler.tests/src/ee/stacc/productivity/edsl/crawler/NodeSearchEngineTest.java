package ee.stacc.productivity.edsl.crawler;

import junit.framework.TestCase;
import ee.stacc.productivity.edsl.crawler.NodeSearchEngine;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Test;

public class NodeSearchEngineTest extends TestCase {
    
	@Test public void testSmth() {
		NodeSearchEngine eng = new NodeSearchEngine();
		//eng.findArgumentNodes(searchScope, requests)
		assertEquals(3, 3);
		assertEquals(3, 4);
		System.out.println(eng);
	}
	
	private IJavaProject getJavaProject(String name) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		if (project instanceof IJavaProject) {
			return (IJavaProject)project;
		}
		else {
			throw new IllegalArgumentException("No such project");
		}
	}

}
