package com.zeroturnaround.alvor.parser;

import static org.junit.Assert.assertEquals;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.zeroturnaround.alvor.sqlparser.BoundedStack;
import com.zeroturnaround.alvor.sqlparser.ILRParser;
import com.zeroturnaround.alvor.sqlparser.IParserStack;
import com.zeroturnaround.alvor.sqlparser.IStackFactory;
import com.zeroturnaround.alvor.sqlparser.Parsers;

@RunWith(Parameterized.class)
public class LRParsingTest {

	@Parameters
	public static Collection<Object[]> parameters() {
		return Arrays.asList(new Object[][] {
//				{SimpleStack.FACTORY},
//				{SimpleLinkedStack.FACTORY},
//				{SimpleFoldedStack.FACTORY},
				{BoundedStack.getFactory(100, null)},
		});
	}

	private final IStackFactory<IParserStack> stackFactory;
	
	public LRParsingTest(IStackFactory<IParserStack> stackFactory) {
		this.stackFactory = stackFactory;
	}

	@Test
	public void testBasics() throws Exception {
		ILRParser<IParserStack> parser = Parsers.getLALRParserForSQL();

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
				true);
		
		interpret(parser, stackFactory, 
				"SELECT FROM ID ',' ID ',' ID", 
				false);
		
		interpret(parser, stackFactory, 
				"SELECT ID '(' NUMBER ')' FROM ID ',' ID ',' ID", 
				true);
		
		interpret(parser, stackFactory, 
				"SELECT ID '(' '*' ')' FROM ID ',' ID ',' ID", 
				true);
		
	}
	
	@Test
	public void testNestedParentheses() throws Exception {
		ILRParser<IParserStack> parser = Parsers.getLALRParserForSQL();

		interpret(parser, stackFactory, 
				"SELECT ID '(' ID '(' ID ')' ')' FROM ID ',' ID ',' ID", 
				true);

	}

	public static boolean interpret(ILRParser<IParserStack> parser, IStackFactory<IParserStack> factory, String input,
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

	private static boolean followParsingTrace(ILRParser<IParserStack> parser, IParserStack stack, List<String> tokens, String trace, boolean expected, PrintStream out) {
		trace += stack;
		if (tokens.isEmpty()) {
			if ((stack.top().isError()) == expected) {
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
		IParserStack newStack = parser.processToken(null, tokenNumber, stack);
		boolean r = followParsingTrace(parser, newStack, tokens.subList(1, tokens.size()), trace + "\n===\n", expected, out);
		if (r != expected) {
			return r;
		}
		return expected;
	}
	
}
