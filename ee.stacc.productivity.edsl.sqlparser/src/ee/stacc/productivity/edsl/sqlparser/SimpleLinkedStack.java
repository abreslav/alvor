/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import java.util.Collections;
import java.util.Set;


public final class SimpleLinkedStack implements IAbstractStack {

	public static final IStackFactory FACTORY = new  IStackFactory() {
		
		@Override
		public IAbstractStack newStack(IParserState state) {
			return new SimpleLinkedStack(state);
		}
	};
	
	private final static class StackEntry {
		private final StackEntry nextEntry;
		private final IParserState state;
		private Integer hashCode = null;
		
		public StackEntry(StackEntry nextEntry, IParserState state) {
			this.nextEntry = nextEntry;
			this.state = state;
		}

		@Override
		public int hashCode() {
			if (hashCode == null) {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((nextEntry == null) ? 0 : nextEntry.hashCode());
				result = prime * result + ((state == null) ? 0 : state.hashCode());
				hashCode = result;
			}
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StackEntry other = (StackEntry) obj;
			if (nextEntry == null) {
				if (other.nextEntry != null)
					return false;
			} else if (!nextEntry.equals(other.nextEntry))
				return false;
			if (state == null) {
				if (other.state != null)
					return false;
			} else if (!state.equals(other.state))
				return false;
			return true;
		}
		
		
	}

	private final StackEntry top;
	
	private SimpleLinkedStack(StackEntry top) {
		this.top = top;
	}

	/*package*/ SimpleLinkedStack(IParserState state) {
		this(new StackEntry(null, state));
	}
	
//	/*package*/ SimpleLinkedStack() {
//		this.stack = new ArrayList<IParserState>();
//	}

	@Override
	public Set<IAbstractStack> pop(int count) {
//		System.out.println(">>> pop " + count + " from " + this);
		StackEntry newTop = top;
		for (int i = 0; i < count; i++) {
			newTop = newTop.nextEntry;
		}
		return Collections.<IAbstractStack>singleton(new SimpleLinkedStack(newTop));
	}

	@Override
	public IAbstractStack push(IParserState state) {
//		System.out.println(">>> push " + state + " into " + this);
		StackEntry newTop = new StackEntry(top, state);
		return new SimpleLinkedStack(newTop);
	}

	@Override
	public IParserState top() {
		return top.state;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[");
		for (StackEntry cur = top; cur != null; cur = cur.nextEntry) {
			builder.append(cur.state).append(" ");
		}
		return builder.append("]").toString();
	}

	@Override
	public int hashCode() {
		return top.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleLinkedStack other = (SimpleLinkedStack) obj;
		if (top == null) {
			if (other.top != null)
				return false;
		} else if (!top.equals(other.top))
			return false;
		return true;
	}
}