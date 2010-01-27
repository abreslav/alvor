package ee.stacc.productivity.edsl.parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.LRParser;

public class LRInterpreter {

	public static IParserState interpret(LRParser parser, IStackFactory factory, String input,
			IParserState expectedState) {
		Map<String, Integer> namesToTokenNumbers = parser.getNamesToTokenNumbers();
		ByteArrayOutputStream traceData = new ByteArrayOutputStream();
		PrintStream trace = new PrintStream(traceData);
		parser.setTrace(trace);
		trace.print("Parsing: " + input + "\n");
		String[] split = input.split(" ");
		trace.append("Input tokens: ");
		for (String token : split) {
			Integer tokenNumber = namesToTokenNumbers.get(token.trim());
			trace.print(tokenNumber + ",");
		}
		trace.append("\n");
		IAbstractStack stack = factory.newStack(parser.getInitialState());
		for (String token : split) {
			Integer tokenNumber = namesToTokenNumbers.get(token.trim());
			trace.append("Token: " + token + "\n");
			trace.append("Stack: " + stack + "\n");
			if (tokenNumber == null) {
				throw new IllegalArgumentException("Unknown token: " + token);
			}
			
			Set<IAbstractStack> stacks = parser.processToken(tokenNumber, stack);
			if (stacks.size() > 1) {
				throw new IllegalStateException("Only simple stacks are supported");
			}
			stack = stacks.iterator().next();
			trace.println();
		}
		IParserState top = stack.top();
		if (top != expectedState) {
			System.out.println(traceData.toString());
		}
		return top;
	}

}
