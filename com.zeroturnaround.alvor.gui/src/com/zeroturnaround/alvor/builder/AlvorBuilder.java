package com.zeroturnaround.alvor.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;
import com.zeroturnaround.alvor.gui.GuiChecker;
import com.zeroturnaround.alvor.gui.GuiUtil;

public class AlvorBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "com.zeroturnaround.alvor.builder.AlvorBuilder";

	private Cache cache = CacheProvider.getCache();
	
	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		
		Timer timer = new Timer("BUILD TIMER");

		// first invalidate files (all or changed)
		if (kind == FULL_BUILD || kind == CLEAN_BUILD) {
			this.clean(monitor);
		}
		else if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
			this.registerFileChanges(this.getDelta(this.getProject()));
		}
		else {
			throw new IllegalArgumentException("Unknown build kind: " + kind);
		}

		// then bring everything back up-to-date
		if (!JavaModelUtil.projectHasJavaErrors(this.getProject())) {
			GuiChecker.INSTANCE.updateProjectMarkers(this.getProject(), monitor);
		}
		else {
			GuiUtil.setStatusbarMessage("Did not check SQL because project has Java errors");
		}
		
		timer.printTime();
		
		
		return null; // TODO what's this?
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
		
		// TODO if .alvor ends up here then clean cache
		// (if .alvor change is not detected, then clean cache after changing in gui)
		
		delta.accept(new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				
				// TODO detect configuration change ???
				
				IResource resource = delta.getResource();
				if (JavaModelUtil.isSourceFile(resource)) {
					String fileName = resource.getFullPath().toPortableString();
					switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						cache.addFile(resource.getProject().getName(), fileName);
						// ADDED could be one step of rename, in this case need to delete markers 
						// that were carried over from old filename (otherwise they get duplicated)
						GuiChecker.deleteAlvorMarkers(resource); 
						break;
					case IResourceDelta.REMOVED:
						cache.removeFile(fileName);
						break;
					case IResourceDelta.CHANGED:
						cache.invalidateFile(fileName);
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
}
