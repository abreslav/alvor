package ee.stacc.productivity.edsl.lexer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class CharacterSetFactory {
//	private static final class Range implements ICharacterSet {
//		private final char from;
//		private final char to;
//
//		private Range(char from, char to) {
//			this.from = from;
//			this.to = to;
//		}
//		
//		@Override
//		public boolean contains(char c) {
//			return (c >= from) && (c <= to);
//		}
//
//		@Override
//		public boolean contains(ICharacterSet set) {
//			return false;
//		}
//	}

	private static final class CSet implements ICharacterSet {
		private final Set<Character> set;
		
		public CSet(Set<Character> set) {
			this.set = Collections.unmodifiableSet(set);
		}
		
		public CSet(char from, char to) {
			Set<Character> s = new HashSet<Character>();
			for (char c = from; c <= to; c++) {
				s.add(c);
			}
			set = Collections.unmodifiableSet(s);
		}
		
		@Override
		public Set<Character> asJavaSet() {
			return set;
		}
		
		@Override
		public boolean contains(char c) {
			return set.contains(c);
		}

		@Override
		public boolean containsSet(ICharacterSet other) {
			return set.containsAll(other.asJavaSet());
		}
		
		@Override
		public ICharacterSet join(ICharacterSet other) {
			Set<Character> otherSet = new HashSet<Character>(other.asJavaSet());
			otherSet.addAll(set);
			return new CSet(otherSet);
		}
		
		@Override
		public boolean intersects(ICharacterSet other) {
			Set<Character> otherSet = new HashSet<Character>(other.asJavaSet());
			otherSet.retainAll(set);
			return !otherSet.isEmpty();
		}
		
		@Override
		public boolean isEmpty() {
			return set.isEmpty();
		}
		
		@Override
		public String toString() {
			return set.toString();
		}
	}
	
	private static ICharacterSet ANY;
	private static ICharacterSet EMPTY;
	
	public static ICharacterSet range(char from, char to) {
		return new CSet(from, to);
	}
	
	public static ICharacterSet set(Set<Character> set) {
		return new CSet(set);
	}
	
	public static ICharacterSet any() {
		if (ANY == null) {
			ANY = new ICharacterSet() {
				
				private Set<Character> all;
				
				@Override
				public ICharacterSet join(ICharacterSet other) {
					return this;
				}
				
				@Override
				public boolean intersects(ICharacterSet other) {
					return !other.isEmpty();
				}
				
				@Override
				public boolean containsSet(ICharacterSet other) {
					return true;
				}
				
				@Override
				public boolean contains(char c) {
					return true;
				}
				
				@Override
				public Set<Character> asJavaSet() {
					if (all == null) {
						all = new HashSet<Character>();
						for (char c = Character.MIN_VALUE; c <= Character.MAX_VALUE; c++) {
							all.add(c);
						}
					}
					return all;
				}
				
				@Override
				public boolean isEmpty() {
					return false;
				}
			};
		}
		return ANY;
	}

	public static ICharacterSet empty() {
		if (EMPTY == null) {
			EMPTY = new ICharacterSet() {
				
				@Override
				public ICharacterSet join(ICharacterSet other) {
					return other;
				}
				
				@Override
				public boolean intersects(ICharacterSet other) {
					return false;
				}
				
				@Override
				public boolean containsSet(ICharacterSet other) {
					return other.isEmpty();
				}
				
				@Override
				public boolean contains(char c) {
					return false;
				}
				
				@Override
				public Set<Character> asJavaSet() {
					return Collections.emptySet();
				}
				
				@Override
				public boolean isEmpty() {
					return true;
				}
			};
		}
		return EMPTY;
	}

}
