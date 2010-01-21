package ee.stacc.productivity.edsl.lexer.automata;

import static org.junit.Assert.*;

import org.junit.Test;

import ee.stacc.productivity.edsl.sqllexer.SQLLexerData;


public class AutomataConverterTest {

	@Test
	public void test() throws Exception {
		State initial = AutomataConverter.INSTANCE.convert();
		
		State current = initial;
		String input = "asd SELECT fdd = 123";
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			char inChar = input.charAt(i);
			System.out.println(inChar);
			char c = SQLLexerData.CHAR_CLASSES[inChar];
			if (c == 0) {
				throw new IllegalArgumentException("Illegal character: '" + inChar + "'");
			}
			for (Transition transition : current.getOutgoingTransitions()) {
				if (!transition.isEmpty() && transition.getInChar() == c) {
					current = transition.getTo();
					String outStr = transition.getOutStr();
					System.out.println("'" + outStr + "'");
					for (int j = 0; j < outStr.length(); j++) {
						output.append(SQLLexerData.TOKENS[outStr.charAt(j)]);
						output.append(" ");
					}
				}
			}
		}
		
		System.out.println(output);
	}
}
