/**
 * 
 */
package com.zeroturnaround.alvor.string;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


public class StringCharacterSet extends PositionedString {

	private final Set<Character> set;
	
	public StringCharacterSet(Collection<Character> set) {
		this(null, set);
	}
	
	public StringCharacterSet(IPosition pos, Collection<Character> set) {
		super(pos);
		this.set = new LinkedHashSet<Character>(set);
	}

	public StringCharacterSet(String set) {
		this(null, set);
	}
	
	public StringCharacterSet(IPosition pos, String set) {
		super(pos);
		Set<Character> hashSet = new LinkedHashSet<Character>();
		for (int i = 0; i < set.length(); i++) {
			hashSet.add(set.charAt(i));
		}
		this.set = hashSet;
	}
	
	public boolean contains(Character c) {
		return set.contains(c);
	}
	
	public boolean containsAll(Collection<Character> c) {
		return set.containsAll(c);
	}
	
	public Set<Character> getContents() {
		return Collections.unmodifiableSet(set);
	}
	
	public String getContentsAsString() {
		StringBuilder b = new StringBuilder();
		for (Character character : getContents()) {
			b.append(character);
		}
		return b.toString();
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("[");
		for (Character c : set) {
			switch (c) {
			case '\\':
				stringBuilder.append("\\\\");
				break;
			case ']':
				stringBuilder.append("\\]");
				break;
			default:
				stringBuilder.append(c);
				break;
			}
		}
		stringBuilder.append(']');
		return stringBuilder.toString();
	}

	public <R, D> R accept(IAbstractStringVisitor<? extends R,? super D> visitor, D data) {
		return visitor.visitStringCharacterSet(this, data);
	};
	
	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean containsRecursion() {
		return false;
	}
}
