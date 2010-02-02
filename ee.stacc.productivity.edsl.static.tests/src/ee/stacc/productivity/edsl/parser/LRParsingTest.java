package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertEquals;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.LRParser;

@RunWith(Parameterized.class)
public class LRParsingTest {

	@Parameters
	public static Collection<Object[]> parameters() {
		return Arrays.asList(new Object[][] {
				{SimpleStack.FACTORY},
				{SimpleFoldedStack.FACTORY},
		});
	}

	private final IStackFactory stackFactory;
	
	public LRParsingTest(IStackFactory stackFactory) {
		this.stackFactory = stackFactory;
	}

	@Test
	public void testBasics() throws Exception {
		LRParser parser = Parsers.SQL_PARSER;

		interpret(parser, stackFactory, 
				"SELECT ID FROM ID", 
				true);
		
		interpret(parser, stackFactory, 
				"SELECT ID ',' ID ',' ID ',' ID ',' ID FROM ID", 
				true);
		
		interpret(parser, stackFactory, 
				"SELECT ID ',' ID ',' ID ',' ID ',' ID FROM ID ',' ID ',' ID", 
				true);
		
		
		interpret(parser, stackFactory, 
				"SELECT ID '.' ID ',' ID ',' ID ',' ID ',' ID FROM ID ',' ID ',' ID", 
				true);
		
		interpret(parser, stackFactory, 
				"SELECT ID ID ',' ID ',' ID ',' ID FROM ID ',' ID ',' ID", 
				false);
		
		interpret(parser, stackFactory, 
				"SELECT FROM ID ',' ID ',' ID", 
				false);
		
		interpret(parser, stackFactory, 
				"SELECT ID '(' ')' FROM ID ',' ID ',' ID", 
				true);
		
		interpret(parser, stackFactory, 
				"SELECT ID '(' '*' ')' FROM ID ',' ID ',' ID", 
				true);
		
	}
	
	@Test
	public void testNestedParentheses() throws Exception {
		LRParser parser = Parsers.SQL_PARSER;

		interpret(parser, stackFactory, 
				"SELECT ID '(' ID '(' ')' ')' FROM ID ',' ID ',' ID", 
				true);

	}

	public static boolean interpret(LRParser parser, IStackFactory factory, String input,
			boolean expected) {
		input += " $end $end"; 
		Map<String, Integer> namesToTokenNumbers = parser.getNamesToTokenNumbers();
		String trace = "Parsing: " + input + "\n";
		String[] split = input.split(" ");
		trace += "Input tokens: ";
		for (String token : split) {
			Integer tokenNumber = namesToTokenNumbers.get(token.trim());
			trace += tokenNumber + ",";
		}
		trace += "\n";
		
		PrintStream out = System.out;//new PrintStream(new ByteArrayOutputStream());
		boolean r = followParsingTrace(parser, factory.newStack(parser.getInitialState()), Arrays.asList(split), trace, expected, out);
		assertEquals(expected, r);
		return r;
	}

	private static boolean followParsingTrace(LRParser parser, IAbstractStack stack, List<String> tokens, String trace, boolean expected, PrintStream out) {
		trace += stack;
		if (tokens.isEmpty()) {
			if ((stack.top() == IParserState.ERROR) == expected) {
				out.println(trace);
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
			boolean r = followParsingTrace(parser, newStack, tokens.subList(1, tokens.size()), trace + "\n===\n", expected, out);
			if (r != expected) {
				return r;
			}
		}
		return expected;
	}
	
}
