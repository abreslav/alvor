package com.zeroturnaround.alvor.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import com.zeroturnaround.alvor.cache.CacheService;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.crawler.NodeSearchEngine;
import com.zeroturnaround.alvor.gui.GuiChecker;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.util.PositionUtil;

public class AlvorBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "com.zeroturnaround.alvor.builder.AlvorBuilder";

	GuiChecker checker = null; // seems I can't initialize it here (?)
	
	private static ILog LOG = Logs.getLog(AlvorBuilder.class);
	private Set<IFile> invalidatedFiles = new HashSet<IFile>();

	private class DeltaVisitor implements IResourceDeltaVisitor {
		private final Collection<IFile> resources = new ArrayList<IFile>();
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource instanceof IFile) {
				switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					addResource(resource);
					break;
				case IResourceDelta.REMOVED:
					removeFromCache(resource);
					break;
				case IResourceDelta.CHANGED:
					removeFromCache(resource);
					addResource(resource);
					break;
				default:
					throw new IllegalArgumentException("Unexpected kind: " + delta.getKind());
				}
				return false; // file doesn't have children
			}
			else if (resource instanceof IFolder) {
				if (JavaCore.create((IFolder) resource) == null) {
					return false; // don't visit non-source folders
				}
				else {
					return true; // will visit children
				}
			}
			else { // project or some other container
				return true;
			}
		}

		private void addResource(IResource resource) {
			if (resource instanceof IFile && JavaCore.create((IFile) resource) != null) {
				resources.add((IFile) resource);
			}
		}

		public Collection<IFile> getResources() {
			return resources;
		};
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		switch (kind) {
		case FULL_BUILD:
			cleanBuild(monitor);
			break;
		case CLEAN_BUILD:
			cleanBuild(monitor);
			break;
		default:
//			cleanBuild(monitor);
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	protected void cleanBuild(final IProgressMonitor monitor) {
		assert LOG.message("==============================");
		assert LOG.message("Clean build on " + getProject());
		clearCache();
		NodeSearchEngine.clearASTCache();
		checkResources(new IJavaElement[] {JavaCore.create(getProject())});
	}

	protected void fullBuild(final IProgressMonitor monitor) {
		assert LOG.message("==============================");
		assert LOG.message("Full build (no files changed?) on " + getProject());
		checkResources(new IJavaElement[] {JavaCore.create(getProject())});
	}
	
	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		assert LOG.message("==============================");
		assert LOG.message("Incremental build on " + getProject());
		
		invalidatedFiles.clear();
		
		Timer overall = new Timer("Overall");
		
		Timer t = new Timer();
		t.start("Delta visitor");
		
		DeltaVisitor visitor = new DeltaVisitor();
		delta.accept(visitor);
		
		// it's supposedly more efficient to remove all files together from cache 
		Set<String> filesToRecheck = new HashSet<String>();
		for (IFile f : invalidatedFiles) {
			// react only to changes in java files (in a source folder)
			if (JavaCore.create(f) instanceof ICompilationUnit) {
				filesToRecheck.add(PositionUtil.getFileString(f));
				NodeSearchEngine.removeASTFromCache(f);
			}
		}
		
//		if (filesToRecheck.isEmpty()) {
//			assert LOG.message("no compilation units to re-check");
//			return;
//		}
		
		assert LOG.message("!!! Files to invalidate (directly): " + filesToRecheck);
		CacheService.getCacheService().removeFiles(filesToRecheck);
		
		t.printTime();
		
		Collection<IFile> resources = visitor.getResources();
		
//		((CacheServiceImpl) CacheService.getCacheService()).dumpLog();
		
		List<IJavaElement> elements = new ArrayList<IJavaElement>();

		t.start("Invalidate Hotspots");
		Collection<IPosition> hotspots = CacheService.getCacheService().getInvalidatedHotspotPositions();
		t.printTimeAndStart("Elements");
		
		assert LOG.message("Invalidated hotspot positions:");
		for (IPosition position : hotspots) {
			assert LOG.message(position);
			elements.add(JavaCore.create(PositionUtil.getFile(position)));
		}
		
		for (IFile file : resources) {
			elements.add(JavaCore.create(file));
		}
		t.printTime();

		for (IJavaElement e : elements) {
			assert LOG.message(e);
		}
		t.start("Search and check");
		checkResources(elements.toArray(new IJavaElement[elements.size()]));
		t.printTime();
		
		overall.printTime();		
	}

	private void checkResources(IJavaElement[] elements) {
		try {
			if (checker == null) {
				checker = new GuiChecker();
			}
			checker.performIncrementalCheck(getProject(), elements);
		} catch (Throwable e) {
			LOG.error("ESQLBuilder.checkResources", e);
		} 
	}

	private void clearCache() {
		CacheService.getCacheService().clearAll();		
	}

	private void removeFromCache(IResource resource) {
		if (resource instanceof IFile) {
			invalidatedFiles.add((IFile)resource);
		}
	}
}