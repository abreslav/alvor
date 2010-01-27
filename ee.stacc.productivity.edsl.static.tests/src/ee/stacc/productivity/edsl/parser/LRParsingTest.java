package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertEquals;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.LRParser;

public class LRParsingTest {

	@Test
	public void test() throws Exception {
		LRParser parser = Parsers.SQL_PARSER;

		interpret(parser, SimpleFoldedStack.FACTORY, 
				"SELECT ID FROM ID", 
				true);
		
		interpret(parser, SimpleFoldedStack.FACTORY, 
				"SELECT ID ',' ID ',' ID ',' ID ',' ID FROM ID", 
				true);
		
		interpret(parser, SimpleFoldedStack.FACTORY, 
				"SELECT ID ',' ID ',' ID ',' ID ',' ID FROM ID ',' ID ',' ID", 
				true);
		
		interpret(parser, SimpleFoldedStack.FACTORY, 
				"SELECT ID ID ',' ID ',' ID ',' ID FROM ID ',' ID ',' ID", 
				false);
		
		interpret(parser, SimpleFoldedStack.FACTORY, 
				"SELECT FROM ID ',' ID ',' ID", 
				false);
	}

	public static boolean interpret(LRParser parser, IStackFactory factory, String input,
			boolean expected) {
		input += " $end $end"; 
		Map<String, Integer> namesToTokenNumbers = parser.getNamesToTokenNumbers();
		PrintStream trace = System.out;
//		parser.setTrace(trace);
		trace.print("Parsing: " + input + "\n");
		String[] split = input.split(" ");
		trace.append("Input tokens: ");
		for (String token : split) {
			Integer tokenNumber = namesToTokenNumbers.get(token.trim());
			trace.print(tokenNumber + ",");
		}
		trace.append("\n");
		
		boolean r = followParsingTrace(parser, factory.newStack(parser.getInitialState()), Arrays.asList(split), "", expected);
		assertEquals(expected, r);
		return r;
	}

	private static boolean followParsingTrace(LRParser parser, IAbstractStack stack, List<String> tokens, String trace, boolean expected) {
		trace += stack;
		if (tokens.isEmpty()) {
			if ((stack.top() == IParserState.ERROR) == expected) {
				System.out.println(trace);
				return !expected;
			}
			return expected;
		}
		trace += "\n===\n" +
				"Current state: " + stack.top() + "\n" + 
				"Tokens: " + tokens;
		String token = tokens.get(0);
		Integer tokenNumber = parser.getNamesToTokenNumbers().get(token.trim());
		Set<IAbstractStack> stacks = parser.processToken(tokenNumber, stack);
		for (IAbstractStack newStack : stacks) {
			boolean r = followParsingTrace(parser, newStack, tokens.subList(1, tokens.size()), trace + "\n===\n", expected);
			if (r != expected) {
				return r;
			}
		}
		return expected;
	}
	
}
