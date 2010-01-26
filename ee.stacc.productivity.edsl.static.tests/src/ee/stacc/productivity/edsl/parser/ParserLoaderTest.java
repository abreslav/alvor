package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.LRParser;


public class ParserLoaderTest {

	@Test
	public void test() throws Exception {
		LRParser parser = LRParser.build("../ee.stacc.productivity.edsl.sqlparser/generated/arith.xml");
		Map<String, Integer> namesToTokenNumbers = parser.getNamesToTokenNumbers();
		
		String[] correctInputs = {
				"'1'",
				"'0'",
				"'1' '*' '0'",
				"'0' '+' '1'",
				"'1' '*' '0' '+' '1' '*' '0'",
				"'1' '+' '0' '+' '1' '*' '0'",
		};
		for (String input : correctInputs) {
			input += " $end $end";
			String[] split = input.split(" ");
			IAbstractStack stack = new SimpleStack(parser.getInitialState());
			for (String token : split) {
				Integer tokenNumber = namesToTokenNumbers.get(token.trim());
				if (tokenNumber == null) {
					throw new IllegalArgumentException("Unknown token: " + token);
				}
				
				Set<IAbstractStack> stacks = parser.processToken(tokenNumber, stack);
				if (stacks.size() > 1) {
					throw new IllegalStateException("Only simple stacks are supported");
				}
				stack = stacks.iterator().next();
			}
			IParserState top = stack.top();
			assertSame(IParserState.ACCEPT, top);
		}
	}
	
	private static final class SimpleStack implements IAbstractStack {

		private final List<IParserState> stack;
		
		private SimpleStack(List<IParserState> stack) {
			this.stack = stack;
		}

		private SimpleStack(IParserState state) {
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
}
