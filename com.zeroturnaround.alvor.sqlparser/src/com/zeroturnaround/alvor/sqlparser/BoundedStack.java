/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A parsing stack of counded depth.
 * If the stack grows bigger than the given number, it is truncated.
 * If the bottom is reached, the error state is returned.
 *  
 * @author abreslav
 *
 */
public final class BoundedStack implements IParserStack {

	/**
	 * A factory method. Use instead of the constructor 
	 * @param maxDepth maximum depth allowed for the stack (the created stacks will initially have the depth 1, 
	 * but if they grow bigger than this number, they will be truncated)
	 * @param errorState the state to "put to the bottom" of the truncated stacks 
	 * @return
	 */
	
	public static IStackFactory<IParserStack> getFactory(final int maxDepth, final IParserState errorState) {
		return new IStackFactory<IParserStack>() {
			@Override
			public IParserStack newStack(IParserState state) {
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
	public IParserState getErrorOnTop() {
		IParserState top = top();
		return top.isError() ? top : null;
	}
	
	@Override
	public boolean hasErrorOnTop() {
		return getErrorOnTop() != null;
	}
	
	@Override
	public boolean topAccepts() {
		return top() == IParserState.ACCEPT;
	}
	
	@Override
	public IParserStack pop(int count) {
//		System.out.println(">>> pop " + count + " from " + this);
		if (count > stack.size()) {
			return new BoundedStack(maxDepth, errorState, errorState);
		}
		List<IParserState> newStack = new ArrayList<IParserState>(stack.subList(0, stack.size() - count));
		return new BoundedStack(maxDepth, errorState, newStack);
	}

	@Override
	public IParserStack push(IParserState state) {
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