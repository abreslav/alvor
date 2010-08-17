package ee.stacc.productivity.edsl.javaproject.builder;

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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.common.logging.Timer;
import ee.stacc.productivity.edsl.crawler.PositionUtil;
import ee.stacc.productivity.edsl.gui.CheckProjectHandler;
import ee.stacc.productivity.edsl.string.IPosition;

public class ESQLBuilder extends IncrementalProjectBuilder {
	
	private static ILog LOG = Logs.getLog(ESQLBuilder.class);
	private Set<String> stringsToRemove = new HashSet<String>();

	private class DeltaVisitor implements IResourceDeltaVisitor {
		private final Collection<IFile> resources = new ArrayList<IFile>();
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource instanceof IFolder) {
				if (JavaCore.create((IFolder) resource) == null) {
					return false;
				}
			}
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
			//return true to continue visiting children.
			return true;
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

	public static final String BUILDER_ID = "ee.stacc.productivity.edsl.javaproject.esqlBuilder";

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
		checkResources(new IJavaElement[] {JavaCore.create(getProject())});
	}

	protected void fullBuild(final IProgressMonitor monitor) {
		assert LOG.message("==============================");
		assert LOG.message("Full build on " + getProject());
		checkResources(new IJavaElement[] {JavaCore.create(getProject())});
	}
	
	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		assert LOG.message("==============================");
		assert LOG.message("Incremental build on " + getProject());
		
		stringsToRemove.clear();
		
		Timer overall = new Timer("Overall");
		
		Timer t = new Timer();
		t.start("Delta visitor");
		
		DeltaVisitor visitor = new DeltaVisitor();
		delta.accept(visitor);
		
		System.out.println("!!! Strs to remove: " + stringsToRemove);
		CacheService.getCacheService().removeFiles(stringsToRemove);
		
		t.printTime();
		
		Collection<IFile> resources = visitor.getResources();
		
//		((CacheServiceImpl) CacheService.getCacheService()).dumpLog();
		
		List<IJavaElement> elements = new ArrayList<IJavaElement>();

		t.start("Inv HS");
		Collection<IPosition> hotspots = CacheService.getCacheService().getInvalidatedHotspotPositions();
		t.printTimeAndStart("Elements");
		
		System.out.println("Invalidated hotspot positions:");
		for (IPosition position : hotspots) {
			System.out.println(position);
			elements.add(JavaCore.create(PositionUtil.getFile(position)));
		}
		
		for (IFile file : resources) {
			elements.add(JavaCore.create(file));
		}
		t.printTime();

		for (IJavaElement e : elements) {
			System.out.println(e);
		}
		t.start("Search and check");
		checkResources(elements.toArray(new IJavaElement[elements.size()]));
		t.printTime();
		
		overall.printTime();		
	}

	private void checkResources(IJavaElement[] elements) {
		try {
			new CheckProjectHandler().performCheck(JavaCore.create(getProject()), elements);
		} catch (Throwable e) {
			LOG.error(e);
		} 
	}

	private void clearCache() {
		CacheService.getCacheService().clearAll();		
	}

	private void removeFromCache(IResource resource) {
		if (resource instanceof IFile) {
			stringsToRemove.add(PositionUtil.getFileString(resource));
//			CacheService.getCacheService().removeFile(PositionUtil.getFileString(resource));
		}
	}
}