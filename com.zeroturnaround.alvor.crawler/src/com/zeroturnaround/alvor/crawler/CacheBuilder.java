package com.zeroturnaround.alvor.crawler;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;

/**
 * Can synchronize cache with Java files 
 * @author Aivar
 *
 */
public class CacheBuilder {
	private static Cache cache = new Cache(); // TODO
	
	public static void updateProject(IJavaProject javaProject) {
		// 0) update inter-project stuff (import patterns)
		// 1) fetch project patterns
		// 2) get list of files with batch-numbers // ones with 0-batch (ie recently changed) first
		// 3) search files for new patterns // if new patterns can't be there 
		//    (eg. according to call index or when new pattern is really new) then just update batch-no
			
	}
	
	public static void cleanBuildProjectCache(IJavaProject javaProject, ProjectConfiguration conf) {
		String projectName = javaProject.getProject().getName(); 
		cache.cleanProject(projectName);
		cache.addPrimaryHotspotPatterns(projectName, conf.getHotspots());
		findAndAddNewHotspotsForProjectUntilFixpoint(javaProject);
	}
	
	public static void updateCacheAfterChange(IJavaProject javaProject, 
			Collection<ICompilationUnit> changedFiles) {
		List<HotspotPattern> patterns = cache.getProjectPatterns(javaProject.getProject().getName());
		for (ICompilationUnit unit: changedFiles) {
			updateChangedCompilationUnit(unit, patterns);
		}
		findAndAddNewHotspotsForProjectUntilFixpoint(javaProject);
	}

	private static void findAndAddNewHotspotsForProjectUntilFixpoint(IJavaProject javaProject) {
		Collection<ICompilationUnit> units = JavaModelUtil.getAllCompilationUnits(javaProject, false);
		List<HotspotPattern> projectNewPatterns = null;
		
		while (true) {
			projectNewPatterns = cache.getNewProjectPatterns(javaProject.getProject().getName());
			// TODO get cache.getNewStringMethodPatterns
			
			// TODO string method patterns should be taken also from dependent projects
			
			if (projectNewPatterns.isEmpty()) {
				break; // found fixpoint
			}
			
			for (ICompilationUnit unit: units) {
				// TODO extract a sublist of patterns according to file's current batch number
				Collection<HotspotPattern> fileNewPatterns = projectNewPatterns;
				
				if (!fileNewPatterns.isEmpty()) {
					findAndAddHotspotsForCompilationUnit(unit, projectNewPatterns);
				}
			}
			
			// TODO string-method patterns should be searched also in required projects
		}
		
		
		// TODO finally update all required projects 
	}
	
	
	/**
	 * Used after changing a file. May result in new patterns that need to be resolved
	 * separately
	 * 
	 * @param unit
	 * @param patterns
	 */
	private static void updateChangedCompilationUnit(ICompilationUnit unit, final List<HotspotPattern> patterns) {
		cache.removeFileStrings(unit.getElementName());
		findAndAddHotspotsForCompilationUnit(unit, patterns);
		cache.removeOrphanedHotspotPatterns(unit.getElementName());
	}
	
	/** 
	 * This method assumes that there is no stale information about this file in cache
	 * 
	 * Visits all method invocations and collects intraprocedural parts of respective abstract values
	 * (including properly defined stubs in method boundaries).
	 * 
	 * Uses resulting abstract values to update cache for this file 
	 * 
	 * @param unit
	 * @param patterns
	 */
	private static void findAndAddHotspotsForCompilationUnit(ICompilationUnit unit, final List<HotspotPattern> patterns) {
		// TODO try parsing in a batch
		ASTNode ast = ASTUtil.parseCompilationUnit(unit, true);
		
		ast.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
				for (HotspotPattern pattern : patterns) {
					if (ASTUtil.invocationCorrespondsToPattern(node, pattern)) {
						Expression hotspot = (Expression)node.arguments().get(pattern.getArgumentIndex());
						NodeDescriptor descriptor = Crawler2.INSTANCE.evaluate(hotspot);
						cache.addHotspot(pattern, descriptor);
					}
				}
				// Don't want to visit children. 
				// In principle there can be another hotspot in an argument, but
				// it would be quite stupid and I think it doesn't deserve extra computation effort
				return false; 
			}
		});
	}
	
}
