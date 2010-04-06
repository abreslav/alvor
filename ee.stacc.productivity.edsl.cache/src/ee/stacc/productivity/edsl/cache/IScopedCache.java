package ee.stacc.productivity.edsl.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author abreslav
 *
 * @param <K> type of keys by which the elements are cached
 * @param <V> type of cached elements
 */
public interface IScopedCache<K, V> {

	/**
	 * 
	 * @param pattern
	 * @return map [fileName -> fileId]
	 */
	Map<String, Integer> getCahcedScope(K key);
	void add(K key, V value);
	void getCachedResultsInScope(Set<Integer> scope, K key, Collection<? super V> values);
	void markScopeAsCached(K key, Set<String> scope);
	
}
