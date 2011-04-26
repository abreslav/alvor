package com.zeroturnaround.alvor.builder;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;
import com.zeroturnaround.alvor.gui.GuiChecker;

public class AlvorBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "com.zeroturnaround.alvor.builder.AlvorBuilder";
	private static final ILog LOG = Logs.getLog(AlvorBuilder.class);

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		
		Timer timer = new Timer("BUILD TIMER");
		IProject[] requiredProjects = getRequiredProjects(this.getProject());

		// first invalidate files (all or changed)
		if (kind == FULL_BUILD || kind == CLEAN_BUILD) {
			System.err.println("CLEAN BUILD for: " + this.getProject().getName());
			this.clean(monitor);
		}
		else if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
			System.err.println("INCREMENTAL BUILD for: " + this.getProject().getName());
			this.registerFileChanges(this.getDelta(this.getProject()));
			
			if (requiredProjects != null) {
				for (IProject p: requiredProjects) {
					this.registerFileChanges(this.getDelta(p));
				}
			}
		}
		else {
			throw new IllegalArgumentException("Unknown build kind: " + kind);
		}

		// then bring everything back up-to-date
		if (!JavaModelUtil.projectHasJavaErrors(this.getProject())) {
			GuiChecker.INSTANCE.updateProjectMarkers(this.getProject(), monitor);
		}
		else {
			// TODO give some other notification, this one gives errors
			// GuiUtil.setStatusbarMessage("Did not check SQL because project has Java errors");
		}
		
		timer.printTime();
		
		return requiredProjects;
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		GuiChecker.INSTANCE.clearProject(this.getProject());
	}
	
	/**
	 * Invalidates data about changed files in cache and removes their Alvor markers 
	 * @param delta
	 * @throws CoreException
	 */
	private void registerFileChanges(IResourceDelta delta) throws CoreException {
		
		// sometimes may happen, then should start again
		if (delta == null) {
			this.clean(null);
			return;
		}
		
		// TODO if .alvor ends up here then clean cache
		// (if .alvor change is not detected, then clean cache after changing in gui)
		final Cache cache = CacheProvider.getCache(this.getProject().getName());
		delta.accept(new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				
				// TODO detect configuration change ???
				
				IResource resource = delta.getResource();
				if (JavaModelUtil.isSourceFile(resource)) {
					String fileName = resource.getFullPath().toPortableString();
					boolean isSameProjectFile = resource.getProject() == AlvorBuilder.this.getProject();
					switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						cache.addFile(fileName, isSameProjectFile);
						assert LOG.message("ADDED: " + fileName);
						
						// ADDED could be one step of rename, in this case need to delete markers 
						// that were carried over from old filename (otherwise they get duplicated)
						GuiChecker.deleteAlvorMarkers(resource); 
						break;
					case IResourceDelta.REMOVED:
						cache.removeFile(fileName);
						assert LOG.message("REMOVED: " + fileName);
						break;
					case IResourceDelta.CHANGED:
						cache.invalidateFile(fileName, isSameProjectFile);
						assert LOG.message("CHANGED: " + fileName);
						GuiChecker.deleteAlvorMarkers(resource);
						break;
					default:
						throw new IllegalArgumentException("Unexpected kind: " + delta.getKind());
					}
					return false; // file doesn't have children resources
				}
				else {
					// visit children only if contains source
					return JavaModelUtil.isSourceContainer(resource);
				}
				
			}
		});
	}
	
	private IProject[] getRequiredProjects(IProject project) {
		IJavaProject javaProject = JavaModelUtil.getJavaProjectFromProject(this.getProject());
		if (javaProject == null) {
			return null;
		}
		Set<IJavaProject> requiredProjects = JavaModelUtil.getAllRequiredProjects(javaProject);
		if (requiredProjects == null || requiredProjects.size() == 0) {
			return null;
		}
		
		
		IProject[] result = new IProject[requiredProjects.size()];
		int i = 0;
		for (IJavaProject jp: requiredProjects) {
			result[i] = jp.getProject();
		}
		return result;
	}
	
	public static ICommand getAlvorBuilder(IProject project) {
		if (!project.isOpen()) {
			return null;
		}
		
		IProjectDescription desc;
		try {
			desc = project.getDescription();
			ICommand[] commands = desc.getBuildSpec();

			for (int i = 0; i < commands.length; ++i) {
				if (commands[i].getBuilderName().equals(AlvorBuilder.BUILDER_ID)) {
					return commands[i];
				}
			}
			return null;
		}
		catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean projectHasAlvorBuilderEnabled(IProject project) {
		ICommand builder = getAlvorBuilder(project);
		return builder != null;
	}
}
