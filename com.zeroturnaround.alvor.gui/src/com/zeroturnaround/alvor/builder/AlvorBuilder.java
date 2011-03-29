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

public class AlvorBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "com.zeroturnaround.alvor.builder.AlvorBuilder";

	private GuiChecker guiChecker = new GuiChecker();
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
		guiChecker.updateProjectMarkers(this.getProject(), monitor);
		
		timer.printTime();
		
		return null; // TODO what's this?
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		cache.clearProject(this.getProject().getName());
		GuiChecker.clearAlvorMarkers(this.getProject());
	}
	
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
						break;
					case IResourceDelta.REMOVED:
						cache.removeFile(fileName);
						break;
					case IResourceDelta.CHANGED:
						cache.invalidateFile(fileName);
						GuiChecker.clearAlvorMarkers(resource);
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
	
	private static boolean projectHasJavaErrors(IProject project) {
		// FIXME
		return false;
	}
}
