package ee.stacc.productivity.edsl.cache;

import java.util.Collection;
import java.util.Set;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;

public interface ICacheService {

	void shutdown();
	
	void clearAll();
	void removeFile(String path);
	void removeFiles(Set<String> paths);
	
	/**
	 * 
	 * @return collections of positions of hotsposts which are invalidated in the cache:
	 * 		some strings on which they depend were removed from cache
	 */
	Collection<IPosition> getInvalidatedHotspotPositions();
	
	
	/**
	 * @param position
	 * @return null, if not cached yet
	 * @throws UnsupportedStringOpEx is the position was marked as unsupported
	 */
	IAbstractString getAbstractString(IPosition position);

	void addAbstractString(IPosition position, IAbstractString result);
	void addUnsupported(IPosition position, String message);

	IScopedCache<IHotspotPattern, IPosition> getHotspotCache();
	IScopedCache<String, IAbstractString> getMethodReturnValueCache();
	
}
