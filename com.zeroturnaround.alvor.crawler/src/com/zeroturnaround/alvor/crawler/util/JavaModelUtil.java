package com.zeroturnaround.alvor.crawler.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.zeroturnaround.alvor.common.WorkspaceUtil;

public class JavaModelUtil {
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
	
	public static ICompilationUnit getCompilationUnitByName(String name) {
		IFile file = WorkspaceUtil.getFile(name);
		return JavaCore.createCompilationUnitFrom(file);
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
	
	public static IJavaProject getJavaProjectFromProject(IProject project) {
		try {
			IJavaProject jp = (IJavaProject)project.getNature(JavaCore.NATURE_ID);
			if (jp == null) {
				throw new IllegalArgumentException("Project " + project + " is not Java project");
			}
			else {
				return jp;
			}
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
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
	
	
	public static Collection<ICompilationUnit> getAllCompilationUnits(IJavaElement element,
			boolean includeBinaries) {
		try {
			return getAllCompilationUnits_(element, includeBinaries);
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}	
	}
	
	private static Collection<ICompilationUnit> getAllCompilationUnits_(IJavaElement element,
				boolean includeBinaries) throws JavaModelException {
		// http://www.vogella.de/articles/EclipseJDT/article.html
		
		Collection<ICompilationUnit> result = new ArrayList<ICompilationUnit>();
		
		if (element instanceof IJavaProject || element instanceof IPackageFragmentRoot) {

			List<IPackageFragment> packageFragments;

			if (element instanceof IJavaProject) {
				packageFragments = Arrays.asList((((IJavaProject) element).getPackageFragments()));
			}
			else if (element instanceof IPackageFragmentRoot) {
				// TODO does getChildren give only immediate subfolders or flat list of packages?
				
				packageFragments = new ArrayList<IPackageFragment>();
				for (IJavaElement child : ((IPackageFragmentRoot) element).getChildren()) {
					if (child instanceof IPackageFragment) {
						packageFragments.add((IPackageFragment)child);
					}
				}
			}
			else {
				throw new IllegalStateException("Can't happen");
			}
			
			for (IPackageFragment pf : packageFragments) {
				if (pf.getKind() == IPackageFragmentRoot.K_SOURCE || includeBinaries) {
					result.addAll(Arrays.asList(pf.getCompilationUnits()));
				}
			}
			
			return result;
		}
		else if (element instanceof IPackageFragment) {
			IPackageFragment pf = (IPackageFragment)element;
			if (pf.getKind() == IPackageFragmentRoot.K_SOURCE || includeBinaries) {
				return Arrays.asList(pf.getCompilationUnits());
			} 
			else {
				return Collections.emptyList();
			}
		}
		else if (element instanceof ICompilationUnit) {
			return Collections.singletonList((ICompilationUnit)element);
		}
		else {
			throw new IllegalArgumentException("Can't find compilation units from " + element.getClass().getName());
		}
	}
	
	public static boolean isSourceFile(IResource res) {
		return JavaCore.create(res) instanceof ICompilationUnit;
	}
	
	public static boolean isSourceFolderOrPackage(IResource resource) {
		IJavaElement element = JavaCore.create(resource); 
		return element != null && (element instanceof IPackageFragment || element instanceof IPackageFragmentRoot);
	}
	
	public static List<String> getCompilationUnitNames(Collection<ICompilationUnit> units) {
		List<String> names = new ArrayList<String>();
		for (ICompilationUnit unit: units) {
			names.add(unit.getElementName());
		}
		
		return names;
	}
}
