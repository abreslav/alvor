package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import com.zeroturnaround.alvor.cache.IScopedCache;

public abstract class CachedSearcher<K, V> {
	
	/*
	 * Subtracts cached files from all files and performs search on resulting files
	 */
	public void performCachedSearch(
			final List<IFile> allFilesInScope, 
			IScopedCache<? super K, V> scopedCache,
			K key, 
			final List<? super V> values) {

		Map<String, Integer> cachedScope = scopedCache.getCachedScope(key);
		List<IJavaElement> scopeToSearchIn = new ArrayList<IJavaElement>();
		Set<String> scopeToSearchInAsStrings = new HashSet<String>();
		Set<Integer> scopeToGetFromCache = new HashSet<Integer>();
		for (IFile file : allFilesInScope) {
			String path = file.getFullPath().toPortableString();
			Integer fileCacheId = cachedScope.get(path);
			if (fileCacheId == null) {
				scopeToSearchIn.add(JavaCore.create(file));
				scopeToSearchInAsStrings.add(path);
			} else {
				scopeToGetFromCache.add(fileCacheId);
			}
		}

		if (!scopeToGetFromCache.isEmpty()) {
			scopedCache.getCachedResultsInScope(scopeToGetFromCache, key, values);
		}
		
		if (!scopeToSearchIn.isEmpty()) {
			performSearchInScope(scopeToSearchIn, key, values);
			scopedCache.markScopeAsCached(key, scopeToSearchInAsStrings);
		}
	}

	protected abstract void performSearchInScope(List<IJavaElement> scopeToSearchIn,
			K key, List<? super V> values);


}
