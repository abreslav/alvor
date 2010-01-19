/**
 * 
 */
package ee.stacc.productivity.edsl.string;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class StringCharacterSet implements IAbstractString {

	private final Set<Character> set;
	
	public StringCharacterSet(Collection<Character> set) {
		this.set = new HashSet<Character>(set);
	}

	public boolean contains(Character c) {
		return set.contains(c);
	}
	
	public boolean containsAll(Collection<Character> c) {
		return set.containsAll(c);
	}

	public String toString() {
		return set.toString();
	}
}