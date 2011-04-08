package com.zeroturnaround.alvor.common;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
}
