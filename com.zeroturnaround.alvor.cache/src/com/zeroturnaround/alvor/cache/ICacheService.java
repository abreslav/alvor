package com.zeroturnaround.alvor.cache;

import java.util.Collection;
import java.util.Set;

import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;

public interface ICacheService {

	void shutdown();
	
	void clearAll();
	void removeFile(String path);
	void removeFiles(Set<String> paths);
	void setNocache(boolean value);
	
	/**
	 * 
	 * @return collection of positions of hotspots which are invalidated in the cache:
	 * 		some strings on which they depend were removed from cache
	 */
	Collection<IPosition> getInvalidatedHotspotPositions();
	
	
	/**
	 * @param position
	 * @return null, if not cached yet
	 * @throws UnsupportedStringOpEx is the position was marked as unsupported
	 */
	IAbstractString getAbstractString(IPosition position);

	/**
	 * Returns the outermost abstract string containing the given offset
	 * @param fileString
	 * @param offset
	 * @return null if nothing found, an abstract string otherwise
	 */
	IAbstractString getContainingAbstractString(String fileString, int offset);

	void addAbstractString(IPosition position, IAbstractString result);
	void addUnsupported(IPosition position, String message);

	IScopedCache<HotspotPattern, IPosition> getHotspotCache();
	IScopedCache<MethodInvocationDescriptor, IAbstractString> getMethodTemplateCache();

	
}
