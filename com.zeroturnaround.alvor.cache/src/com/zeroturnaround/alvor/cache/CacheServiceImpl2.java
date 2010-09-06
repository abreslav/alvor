package com.zeroturnaround.alvor.cache;
/*
 * After a file is changed:
 * - remove from cache hotspots of this file and respective abstract strings
 *   (components of as-s with positions in other files can stay)
 * 
 * - re-search hotspots and recollect abstract strings (possibly reusing the components in other files)
 * 
 * - HARD PART: recompute parts of abstract strings required by other files
 *   (ie. abstract method definitions and callsite argument expressions)
 *   for this, cache should have some higher level info besides positions (names)
 *   
 *    
 * TODO: instead invalidating files, invalidate source-ranges   
 */

import java.util.Collection;
import java.util.Set;

import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;

public class CacheServiceImpl2 implements ICacheService {

	@Override
	public void shutdown() {
	}

	@Override
	public void clearAll() {
	}

	@Override
	public void removeFile(String path) {
	}

	@Override
	public void removeFiles(Set<String> paths) {
	}

	@Override
	public void setNocache(boolean value) {
	}

	@Override
	public Collection<IPosition> getInvalidatedHotspotPositions() {
		return null;
	}

	@Override
	public IAbstractString getAbstractString(IPosition position) {
		return null;
	}

	@Override
	public IAbstractString getContainingAbstractString(String fileString,
			int offset) {
		return null;
	}

	@Override
	public void addAbstractString(IPosition position, IAbstractString result) {
	}

	@Override
	public void addUnsupported(IPosition position, String message) {
	}

	@Override
	public IScopedCache<IHotspotPattern, IPosition> getHotspotCache() {
		return null;
	}

	@Override
	public IScopedCache<MethodInvocationDescriptor, IAbstractString> getMethodTemplateCache() {
		return null;
	}

}
