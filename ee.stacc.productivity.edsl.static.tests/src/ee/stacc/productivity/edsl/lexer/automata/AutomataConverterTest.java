package ee.stacc.productivity.edsl.lexer.automata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ee.stacc.productivity.edsl.sqllexer.SQLLexerData;


public class AutomataConverterTest {

	@Test
	public void test() throws Exception {
		State initial = AutomataConverter.INSTANCE.convert();
		
		String input;
		String expected;
		StringBuilder output;

		input = "aaaabdexaacdedexabdexxxx";
		expected = "ID";
		
		output = interpret(initial, input);
		assertEquals(expected, output.toString().trim());


		input = "SELECT t from 1 as = seleCt";
		expected = "SELECT WS ID WS FROM WS NUMBER WS AS WS = WS SELECT";
		
		output = interpret(initial, input);
		assertEquals(expected, output.toString().trim());

		
		input = "'sad\"fsa'   \"sadf a asd f' adsfsdf \"   sdfasd";
		expected = "STRING_SQ WS STRING_DQ WS ID";
		
		output = interpret(initial, input);
		assertEquals(expected, output.toString().trim());
		
		
		input = "'sad\"fsa'   \"sadf a asd f' adsfsdf    sdfasd";
		expected = "STRING_SQ WS STRING_DQ_ERR";
		
		output = interpret(initial, input);
		assertEquals(expected, output.toString().trim());
		
		
		input = "SELECT cc.ColumnName FROM AD_Column c";
		expected = "SELECT WS ID . ID WS FROM WS ID WS ID";
		
		output = interpret(initial, input);
		assertEquals(expected, output.toString().trim());
		
		
		input = "INSERT INTO X_Test(Text1, Text2) values(?,?)";
		expected = "INSERT WS INTO WS ID ( ID , WS ID ) WS VALUES ( ? , ? )";
		
		output = interpret(initial, input);
		assertEquals(expected, output.toString().trim());
		
	}

	private StringBuilder interpret(State initial, String input) {
		State current = initial;
		StringBuilder output = new StringBuilder();
		for (int i = 0; i <= input.length(); i++) {
			int inChar = (i < input.length()) ? input.charAt(i) : -1;
			int c = inChar < 0 ? inChar : SQLLexerData.CHAR_CLASSES[inChar];
			if (c == 0) {
				throw new IllegalArgumentException("Illegal character: '" + ((char) inChar) + "'");
			}
			boolean worked = false;
			for (Transition transition : current.getOutgoingTransitions()) {
				if (!transition.isEmpty() && transition.getInChar() == c) {
					current = transition.getTo();
					String outStr = transition.getOutStr();
					for (int j = 0; j < outStr.length(); j++) {
						output.append(SQLLexerData.TOKENS[outStr.charAt(j)]);
						output.append(" ");
					}
					worked = true;
					break;
				}
			}
			if (!worked) {
				throw new IllegalArgumentException("Impossible character: '" + ((char) inChar) + "' in state " + current); 
			}
		}
		if (!current.isAccepting()) {
			throw new IllegalArgumentException("Abnoramlly terminated in state: " + current);
		}
		return output;
	}
}
