package ee.stacc.productivity.edsl.crawler;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class EclipseUtil {
	public static Set<IJavaProject> getAllRequiredProjects(IJavaProject project) {
		Set<IJavaProject> reqs = new HashSet<IJavaProject>();
		try {
			for (String reqName : project.getRequiredProjectNames()) {
				IJavaProject reqProject = getJavaProjectByName(reqName); 
				reqs.add(reqProject);
				// also add recursive requirements
				reqs.addAll(getAllRequiredProjects(reqProject));
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}		
		
		return reqs;
	}
	
	public static IJavaProject getJavaProjectByName(String name) {
		IWorkspaceRoot wr = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = wr.getProject(name);
		if (project == null) {
			throw new IllegalArgumentException("Project " + name + " not found");
		}
		
		IJavaProject javaProject = null;
		try {
			javaProject = (IJavaProject)project.getNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		if (javaProject == null) {
			throw new IllegalArgumentException("Project " + name + " not a java project");
		}
		
		return javaProject;		
	}
	
	public static IJavaElement[] scopeToProjectAndRequiredProjectsScope(IJavaElement[] scope) {
		Set<IJavaProject> projects = new HashSet<IJavaProject>();
		for (IJavaElement element : scope) {
			IJavaProject project = element.getJavaProject();
			projects.add(project);
			projects.addAll(getAllRequiredProjects(project));
		}
		return projects.toArray(new IJavaElement[projects.size()]);
	}
	

}
