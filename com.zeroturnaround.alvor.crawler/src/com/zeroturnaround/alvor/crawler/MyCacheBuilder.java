package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.FileRecord;
import com.zeroturnaround.alvor.cache.PatternRecord;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.configuration.ConfigurationManager;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;

public class MyCacheBuilder {
	private static Cache cache = Cache.getInstance();
	
	public void fullBuildProject(IProject project, IProgressMonitor monitor) {
		cleanProject(project, monitor);
		updateProjectPrimaryPatterns(project);
		populateCacheWithFilesInfo(project);
		updateProjectCache(project, monitor);
		// TODO setup primary patterns
	}
	
	private void registerFileChanges(IResourceDelta delta) throws CoreException {
		delta.accept(new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				
				// TODO detect configuration change ???
				
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
	
	public void updateProjectPrimaryPatterns(IProject project) {
		ProjectConfiguration conf = ConfigurationManager.readProjectConfiguration(project, true);
		cache.setProjectPrimaryPatterns(project.getName(), conf.getHotspotPatterns());
	}
	
	private void updateProjectCache(IProject project, IProgressMonitor monitor) {
		// 0) TODO update inter-project stuff (import patterns)
		
		//IJavaProject javaProject = JavaModelUtil.getJavaProjectFromProject(this.getProject()); 
		String projectName = project.getName();
		
		while (true) {
			List<PatternRecord> patterns = cache.getNewProjectPatterns(projectName);
			if (patterns.isEmpty()) { // found fixpoint
				break; 
			}
			
			List<FileRecord> fileRecords = cache.getFilesToUpdate(projectName);
			for (FileRecord rec : fileRecords) {
				ICompilationUnit unit = JavaModelUtil.getCompilationUnitByName(rec.getName());
				updateCompilationUnitCache(unit, patterns, rec.getBatchNo());
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
	private void updateCompilationUnitCache(ICompilationUnit unit, 
			final List<PatternRecord> patterns, int currentBatchNo) {
		// TODO try parsing in a batch
		ASTNode ast = ASTUtil.parseCompilationUnit(unit, true);
		
		
		// separate relevant patterns
		final List<PatternRecord> relevantHotspotPatterns = new ArrayList<PatternRecord>();
		final List<PatternRecord> relevantMethodPatterns = new ArrayList<PatternRecord>();
		for (PatternRecord pattern : patterns) {
			// TODO distinguish between hotspot and method patterns
			if (pattern.getBatchNo() > currentBatchNo) {
				relevantHotspotPatterns.add(pattern);
			}
		}
		
		// TODO if all new patterns are new original methods, then there's no point to search it in all files
		// because they couldn't have been called there. Ie. here's an opportunity for optimization
		
		ast.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
				for (PatternRecord pattern : relevantHotspotPatterns) {
//					if (ASTUtil.invocationCorrespondsToPattern(node, pattern)) {
//						Expression hotspot = (Expression)node.arguments().get(pattern.getArgumentIndex());
//						NodeDescriptor descriptor = Crawler2.INSTANCE.evaluate(hotspot);
//						cache.addHotspot(pattern, descriptor);
//					}
				}
				// Don't want to visit children. 
				// In principle there can be another hotspot in an argument, but
				// for now I think it doesn't deserve extra computation effort
				// TODO test how big is the extra computation effort 
				return false; 
			}
			
			@Override
			public boolean visit(MethodDeclaration node) {
				for (PatternRecord pattern : relevantMethodPatterns) {
//					if (ASTUtil.declarationCorrespondsToPattern(node, pattern)) {
//						// TODO
//						// cache.addMethodSummary(pattern, descriptor);
//					}
				}
				return false;
			}
		});
	}

	public void cleanProject(IProject project, IProgressMonitor monitor) {
		cache.clearProject(project.getName());
	}

	private void populateCacheWithFilesInfo(IProject project) {
		Collection<ICompilationUnit> units = JavaModelUtil.getAllCompilationUnits
		(JavaModelUtil.getJavaProjectFromProject(project), false);
		cache.addFiles(project.getName(), JavaModelUtil.getCompilationUnitNames(units));
	}

	public void incrementalBuildProject(IProject project, IResourceDelta delta, IProgressMonitor monitor) {
		try {
			registerFileChanges(delta);
			updateProjectCache(project, monitor);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

}
