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
 * 
 * What can happen when file is changed:
 * 	- interprocedural stuff
 * 		* new call-sites appear to some method in another (unchanged) file containing hotspot ...
 * 		* ... or such call-sites can disappear
 * 		* a method called from other files can change:
 * 			- if it disappears then code breaks and we can expect user to modify other files soon,
 *            therefore we substitute lost a-string with some dummy string ...
 *          - ... but if it is an overridden method then you can just remove this option ???
 *          - it method content changes then only method string in cache should be updated (or invalidated)  
 *  - intraprocedural stuff, can be resolved by searching and evaluating hotspots in this file 
 *      * hotspots may appear or disappear
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
