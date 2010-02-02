package ee.stacc.productivity.edsl.tests.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ee.stacc.productivity.edsl.lexer.automata.AutomataUtils;
import ee.stacc.productivity.edsl.lexer.automata.ICharacterMapper;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.AutomataUtils.IOutput;

public class TestUtil {

//	public static List<String> generate(State state, String out) {
//		return generate(state, out, AutomataUtils.SQL_TOKEN_MAPPER);
//	}
	
	public static List<String> generate(State state, String out, ICharacterMapper outputMapper) {
		final List<String> result = new ArrayList<String>();
		IOutput output = new IOutput() {
			@Override
			public void putString(String str) {
				result.add(str);
			}
		};
		AutomataUtils.generate(state, out, outputMapper, output);
		return result;
	}

	public static void checkGeneratedSQLStrings(State transduction, String... expected) {
		checkGeneratedStrings(transduction, AutomataUtils.SQL_TOKEN_MAPPER, expected);
	}

	public static void checkGeneratedcharacterStrings(State transduction, String... expected) {
		checkGeneratedStrings(transduction, new ICharacterMapper() {
			
			@Override
			public String map(int c) {
				if (c == -1) {
					return "EOF";
				}
				return "" + (char) c;
			}
		}, expected);
	}
	
	private static void checkGeneratedStrings(State transduction,
			ICharacterMapper outputMapper, String... expected) {
		List<String> generate = generate(transduction, "", outputMapper);
		Set<String> actual = new HashSet<String>();
		for (String string : generate) {
//			System.out.println("\"" + string + "\",");
			actual.add(string.replaceAll(" ", ""));
		}
		for (String expectedStr : expected) {
			assertTrue("Not found: " + expectedStr, actual.contains(expectedStr.replaceAll(" ", "") + "EOF"));
		}
		assertEquals(actual.toString(), new HashSet<String>(Arrays.asList(expected)).size(), actual.size());
	}

}
