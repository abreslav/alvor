package com.googlecode.alvor.tests.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.googlecode.alvor.lexer.automata.AbstractCharacterMapper;
import com.googlecode.alvor.lexer.automata.AutomataUtils;
import com.googlecode.alvor.lexer.automata.ICharacterMapper;
import com.googlecode.alvor.lexer.automata.IInputToString;
import com.googlecode.alvor.lexer.automata.State;
import com.googlecode.alvor.lexer.automata.AutomataUtils.IOutput;

public class TestUtil {

	public static List<String> generate(State state, IInputToString outputMapper) {
		final List<String> result = new ArrayList<String>();
		IOutput output = new IOutput() {
			@Override
			public void putString(String str) {
				result.add(str);
			}
		};
		AutomataUtils.generate(state, outputMapper, output);
		return result;
	}

	public static void checkGeneratedSQLStrings(State transduction, String... expected) {
		checkGeneratedStrings(transduction, AutomataUtils.SQL_TOKEN_MAPPER, expected);
	}

	public static void checkGeneratedcharacterStrings(State transduction, String... expected) {
		checkGeneratedStrings(transduction, new AbstractCharacterMapper() {
			
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
		List<String> generate = generate(transduction, outputMapper);
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
