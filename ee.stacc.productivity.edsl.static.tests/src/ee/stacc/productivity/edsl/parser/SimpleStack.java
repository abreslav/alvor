/**
 * 
 */
package ee.stacc.productivity.edsl.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;

public final class SimpleStack implements IAbstractStack {

	private final List<IParserState> stack;
	
	private SimpleStack(List<IParserState> stack) {
		this.stack = stack;
	}

	SimpleStack(IParserState state) {
		this.stack = new ArrayList<IParserState>();
		stack.add(state);
	}
	
	@Override
	public Set<IAbstractStack> pop(int count) {
		return Collections.<IAbstractStack>singleton(
				new SimpleStack(
						new ArrayList<IParserState>(stack.subList(0, stack.size() - count))));
	}

	@Override
	public Set<IAbstractStack> push(IParserState state) {
		ArrayList<IParserState> newStack = new ArrayList<IParserState>(stack);
		newStack.add(state);
		return Collections.<IAbstractStack>singleton(
				new SimpleStack(
						newStack));
	}

	@Override
	public IParserState top() {
		return stack.get(stack.size() - 1);
	}

	@Override
	public String toString() {
		return stack.toString();
	}
}