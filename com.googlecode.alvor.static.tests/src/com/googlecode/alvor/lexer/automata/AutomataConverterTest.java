package com.googlecode.alvor.lexer.automata;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.googlecode.alvor.lexer.alphabet.IAbstractOutputItem;
import com.googlecode.alvor.lexer.alphabet.Yield;
import com.googlecode.alvor.sqllexer.GenericSQLLexerData;


public class AutomataConverterTest {

	@Test
	public void test() throws Exception {
		
		AbstractLexer lexer = new AbstractLexer(GenericSQLLexerData.DATA);
		
		State initial = lexer.transducer;
		
		String input;
		String expected;
		StringBuilder output;

		input = "aaaabdexaacdedexabdexxxx";
		expected = "ID EOF";
		
		output = interpret(initial, input, lexer.getLexerData());
		assertEquals(expected, output.toString().trim());


		input = "SELECT t from 1 as = seleCt";
		expected = "ID  ID  ID  NUMBER  ID  =  ID EOF";
		
		output = interpret(initial, input, lexer.getLexerData());
		assertEquals(expected, output.toString().trim());

		
		input = "'sad\"fsa' ''  'sadf a asd f'' adsfsdf'   sdfasd";
		expected = "STRING_SQ  STRING_SQ  STRING_SQ  ID EOF";
		
		output = interpret(initial, input, lexer.getLexerData());
		assertEquals(expected, output.toString().trim());
		
		
		input = "'sad\"fsa'   \"sadf a asd f' adsfsdf    sdfasd";
		expected = "STRING_SQ  UNKNOWN_CHARACTER_ERR ID  ID  ID  ID STRING_SQ_ERR EOF";
		
		output = interpret(initial, input, lexer.getLexerData());
		assertEquals(expected, output.toString().trim());
		
		
		input = "SELECT cc.ColumnName FROM AD_Column c";
		expected = "ID  ID . ID  ID  ID  ID EOF";
		
		output = interpret(initial, input, lexer.getLexerData());
		assertEquals(expected, output.toString().trim());
		
		
		input = "INSERT INTO X_Test(Text1, Text2) values(?,?)";
		expected = "ID  ID  ID ( ID ,  ID )  ID ( ? , ? ) EOF";
		
		output = interpret(initial, input, lexer.getLexerData());
		assertEquals(expected, output.toString().trim());
		
	}

	private StringBuilder interpret(State initial, String input, LexerData lexerData) {
		State current = initial;
		StringBuilder output = new StringBuilder();
		for (int i = 0; i <= input.length(); i++) {
			int inChar = (i < input.length()) ? input.charAt(i) : -1;
			int c = inChar < 0 ? inChar : lexerData.charClasses[inChar];
//			if (c == 0) {
//				throw new IllegalArgumentException("Illegal character: '" + ((char) inChar) + "'");
//			}
			boolean worked = false;
			for (Transition transition : current.getOutgoingTransitions()) {
				if (!transition.isEmpty() && transition.getInChar().getCode() == c) {
					current = transition.getTo();
					List<IAbstractOutputItem> outStr = transition.getOutput();
					for (IAbstractOutputItem item : outStr) {
						if (item instanceof Yield) {
							Yield yield = (Yield) item;
							int type = yield.getTokenType();
							if (type == -1) {
								output.append("EOF");
							} else {
								output.append(lexerData.tokens[type]);
							}
							output.append(" ");
						}
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
