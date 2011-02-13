package com.zeroturnaround.alvor.crawler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.FileRecord;
import com.zeroturnaround.alvor.cache.PatternRecord;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;

public class CacheBuilder extends IncrementalProjectBuilder {
	private static Cache cache = new Cache(); // TODO
	
	// FIXME think about error handling
	
	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		
		// update cache about files that need to be processed again
		if (kind == FULL_BUILD || kind == CLEAN_BUILD) {
			this.clean(monitor);
			populateCacheWithFilesInfo();
		}
		else if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
			registerFileChanges(getDelta(getProject()));
		}
		else {
			throw new IllegalArgumentException("Unknown build kind: " + kind);
		}
		
		// recompute strings and other stuff for invalidated/new files 
		updateProjectCache(monitor);
		
		return null; // TODO what's this?
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		cache.clearProject(this.getProject().getName());
	}
	
	private void populateCacheWithFilesInfo() {
		Collection<ICompilationUnit> units = JavaModelUtil.getAllCompilationUnits
		(JavaModelUtil.getJavaProjectFromProject(this.getProject()), false);
		cache.addFiles(this.getProject().getName(), JavaModelUtil.getCompilationUnitNames(units));
	}

	private void registerFileChanges(IResourceDelta delta) throws CoreException {
		delta.accept(new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				if (JavaModelUtil.isSourceFile(resource)) {
					switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						cache.addFile(resource.getProject().getName(), resource.getName());
						break;
					case IResourceDelta.REMOVED:
						cache.removeFile(resource.getName());
						break;
					case IResourceDelta.CHANGED:
						cache.invalidateFile(resource.getName());
						break;
					default:
						throw new IllegalArgumentException("Unexpected kind: " + delta.getKind());
					}
					return false; // file doesn't have children resources
				}
				else {
					// visit children only if contains source
					return JavaModelUtil.isSourceFolderOrPackage(resource);
				}
			}
		});
	}
	
	private void updateProjectCache(IProgressMonitor monitor) {
		// 0) TODO update inter-project stuff (import patterns)
		
		//IJavaProject javaProject = JavaModelUtil.getJavaProjectFromProject(this.getProject()); 
		String projectName = this.getProject().getName();
		
		while (true) {
			List<PatternRecord> patterns = cache.getNewProjectPatterns(projectName);
			if (patterns.isEmpty()) { // found fixpoint
				break; 
			}
			
			List<FileRecord> fileRecords = cache.getFilesToUpdate(projectName);
			for (FileRecord rec : fileRecords) {
				ICompilationUnit unit = JavaModelUtil.getCompilationUnitByName(rec.getName());
				findAndAddHotspotsForCompilationUnit(unit, patterns, rec.getBatchNo());
			}
		}
	}
	
	/** 
	 * This method assumes that there is no stale information about this file in cache,
	 * but there is missing information about some patterns (ie patterns with batchNo bigger than currentBatchNo)
	 * 
	 * Visits all method invocations and collects intraprocedural parts of respective abstract values
	 * (including properly defined stubs in method boundaries).
	 * 
	 * Uses resulting abstract values to update cache for this file 
	 * 
	 * @param unit
	 * @param patterns
	 * @param currentBatchNo indicates biggest pattern batch number this file already has been processed for
	 */
	private static void findAndAddHotspotsForCompilationUnit(ICompilationUnit unit, 
			final List<PatternRecord> patterns, int currentBatchNo) {
		// TODO try parsing in a batch
		ASTNode ast = ASTUtil.parseCompilationUnit(unit, true);
		
		// TODO if all new patterns are new original methods, then there's no point to search it in all files
		// because they couldn't have been called there. Ie. here's an opportunity for optimization
		
		ast.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
				for (PatternRecord pattern : patterns) {
					//if (ASTUtil.invocationCorrespondsToPattern(node, pattern)) {
						Expression hotspot = (Expression)node.arguments().get(pattern.getArgumentIndex());
						NodeDescriptor descriptor = Crawler2.INSTANCE.evaluate(hotspot);
						cache.addHotspot(pattern, descriptor);
					//}
				}
				// Don't want to visit children. 
				// In principle there can be another hotspot in an argument, but
				// it would be quite stupid and I think it doesn't deserve extra computation effort
				return false; 
			}
		});
	}
}
