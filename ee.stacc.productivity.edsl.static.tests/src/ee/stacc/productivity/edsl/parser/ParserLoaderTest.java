package ee.stacc.productivity.edsl.parser;

import static org.junit.Assert.assertSame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import org.jdom.JDOMException;
import org.junit.Test;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.LRParser;


public class ParserLoaderTest {

	@Test
	public void test() throws Exception {
		testParser("../ee.stacc.productivity.edsl.sqlparser/generated/arith.xml", new String[] {
				"'1'",
				"'0'",
				"'1' '*' '0'",
				"'0' '+' '1'",
				"'1' '*' '0' '+' '1' '*' '0'",
				"'1' '+' '0' '+' '1' '*' '0'",
		}, IParserState.ACCEPT);

		testParser("../ee.stacc.productivity.edsl.sqlparser/generated/arith.xml", new String[] {
				"'*'",
				"'+'",
				"'1' '*'",
				"'+' '1'",
				"'1' '*' '0' '1' '*' '0'",
				"'1' '+' '+' '1' '*' '0'",
		}, IParserState.ERROR);
		
		testParser("../ee.stacc.productivity.edsl.sqlparser/generated/sql.xml", new String[] {
				"SELECT ID FROM ID",
				"SELECT ID ',' ID FROM ID",
				"SELECT ID ',' ID FROM ID ',' ID",
				"SELECT ID FROM ID ',' ID",
		}, IParserState.ACCEPT);
		
		testParser("../ee.stacc.productivity.edsl.sqlparser/generated/sql.xml", new String[] {
				"SELECT",
				"FROM",
				"SELECT FROM",
				"SELECT FROM ID",
				"SELECT ID FROM",
				"SELECT ID ID FROM ID",
				"SELECT ',' ID FROM ID",
				"SELECT ID ',' FROM ID",
				"SELECT ID ',' ID FROM ID ID",
				"SELECT ID FROM ',' ID",
				"SELECT ID FROM ','",
		}, IParserState.ERROR);
	}

	private void testParser(String xmlFile, String[] correctInputs, IParserState expectedState)
			throws JDOMException, IOException {
		LRParser parser = LRParser.build(xmlFile);
		Map<String, Integer> namesToTokenNumbers = parser.getNamesToTokenNumbers();
		for (String input : correctInputs) {
			input += " $end $end";
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
			IAbstractStack stack = new SimpleStack(parser.getInitialState());
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
			assertSame(expectedState, top);
		}
	}
}
