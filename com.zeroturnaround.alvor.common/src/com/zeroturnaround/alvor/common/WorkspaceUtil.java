package com.zeroturnaround.alvor.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class WorkspaceUtil {

	public static IProject getProject(String projectName) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (!project.isOpen()) {
			try {
				project.open(null);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
		return project;
	}
	
	
	public static IFile getFile(String name) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.getFile(new Path(name));
	}
	
	
	public static List<IFile> getAllFilesInContainer(IContainer container, Pattern namePattern) {
		List<IFile> files = new ArrayList<IFile>();
		collectAllFilesWithin(container, namePattern, files);
		return files;
	}
	
	private static void collectAllFilesWithin(IContainer container, Pattern namePattern, List<IFile> files) {
		try {
			for (IResource member : container.members()) {
				if (member instanceof IFile && 
						(namePattern == null || namePattern.matcher(member.getName()).matches())) {
					files.add((IFile)member);
					
				}
				else if (member instanceof IContainer) {
					collectAllFilesWithin((IContainer)member, namePattern, files);
				}
			}
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
//	public static Collection<IFile> getAllSourceFilesInContainer(IContainer project) {
//		final Collection<IFile> result = new ArrayList<IFile>();
//		
//		IResourceVisitor visitor = new IResourceVisitor() {
//			@Override
//			public boolean visit(IResource resource) throws CoreException {
//				if (resource.isPhantom() || resource.isHidden() || resource.isTeamPrivateMember()) {
//					return false;
//				}
//				if (resource.getType() == IResource.FILE
//						// TODO check this
//						&& "java".equals(resource.getFileExtension())) {
//					result.add((IFile) resource);
//				}
//				else if (resource instanceof IProject) {
//					return true;
//				}
//				else if (resource instanceof I)
//			}
//		};
//
//	}
	
	public static List<String> getMarkersAsStrings(IProject project, String markerId) {
		try {
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
		catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean getAutoBuilding() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		return description.isAutoBuilding();
	}
	
	public static void setAutoBuilding(boolean value) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		if (description.isAutoBuilding() != value) {
			description.setAutoBuilding(value);
			try {
				workspace.setDescription(description);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
