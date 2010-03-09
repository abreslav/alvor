/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public final class BoundedStack implements IAbstractStack {

	public static IStackFactory getFactory(final int maxDepth, final IParserState errorState) {
		return new IStackFactory() {
			@Override
			public IAbstractStack newStack(IParserState state) {
				return new BoundedStack(maxDepth, errorState, state);
			}
		};
	}
	
	private final int maxDepth; 
	private final IParserState errorState; 
	private final List<IParserState> stack;
	
	private BoundedStack(int maxDepth, IParserState errorState, IParserState state) {
		this(maxDepth, errorState, Collections.singletonList(state));
	}
	
	private BoundedStack(int maxDepth, IParserState errorState, List<IParserState> stack) {
		this.maxDepth = maxDepth;
		this.errorState = errorState;
		this.stack = stack;
	}
	
	@Override
	public Set<IAbstractStack> pop(int count) {
//		System.out.println(">>> pop " + count + " from " + this);
		if (count > stack.size()) {
			return Collections.<IAbstractStack>singleton(new BoundedStack(maxDepth, errorState, errorState));
		}
		List<IParserState> newStack = new ArrayList<IParserState>(stack.subList(0, stack.size() - count));
		return Collections.<IAbstractStack>singleton(new BoundedStack(maxDepth, errorState, newStack));
	}

	@Override
	public IAbstractStack push(IParserState state) {
//		System.out.println(">>> push " + state + " into " + this);
		int start = (stack.size() >= maxDepth) ? 1 : 0;
		List<IParserState> newStack = new ArrayList<IParserState>(stack.subList(start, stack.size()));
		newStack.add(state);
		return new BoundedStack(maxDepth, errorState, newStack);
	}

	@Override
	public IParserState top() {
		if (stack.isEmpty()) {
			return errorState;
		}
		return stack.get(stack.size() - 1);
	}

	@Override
	public String toString() {
		return stack.toString();
	}

	@Override
	public int hashCode() {
		return stack.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoundedStack other = (BoundedStack) obj;
		if (stack == null) {
			if (other.stack != null)
				return false;
		} else if (!stack.equals(other.stack))
			return false;
		return true;
	}
}