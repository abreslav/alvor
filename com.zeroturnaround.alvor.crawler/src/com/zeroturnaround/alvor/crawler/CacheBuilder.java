package com.zeroturnaround.alvor.crawler;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CacheBuilder extends IncrementalProjectBuilder {
	
	// FIXME think about error handling
	FullParseCacheBuilder helper = new FullParseCacheBuilder();
	
	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		
		// update cache about files that need to be processed again
		if (kind == FULL_BUILD || kind == CLEAN_BUILD) {
			helper.fullBuildProject(this.getProject(), monitor);
		}
		else if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
			helper.incrementalBuildProject(this.getProject(), this.getDelta(this.getProject()), monitor);
		}
		else {
			throw new IllegalArgumentException("Unknown build kind: " + kind);
		}
		
		return null; // TODO what's this?
	}
	
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		helper.cleanProject(this.getProject(), monitor);
	}
	
}
