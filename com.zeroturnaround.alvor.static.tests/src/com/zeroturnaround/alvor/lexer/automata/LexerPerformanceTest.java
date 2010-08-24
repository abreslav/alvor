package com.zeroturnaround.alvor.lexer.automata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.Test;

import com.zeroturnaround.alvor.checkers.sqlstatic.SyntacticalSQLChecker;
import com.zeroturnaround.alvor.lexer.sql.SQLLexer;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;
import com.zeroturnaround.alvor.string.parser.AbstractStringParser;
import com.zeroturnaround.alvor.tests.util.TestUtil;


public class LexerPerformanceTest {
	
	@Test
	public void testShorts() throws Exception {
		List<IAbstractString> strings = AbstractStringParser.parseFile("data/sqls.txt");
		
		Map<String, IInputToString> expectedMap = new LinkedHashMap<String, IInputToString>();
		expectedMap.put("data/sqls.expected", AutomataUtils.SQL_TOKEN_MAPPER);
		expectedMap.put("data/sqls.tokens.expected", AutomataUtils.SQL_TOKEN_TO_STRING);
		
		Map<String, Set<String>> generatedMap = new LinkedHashMap<String, Set<String>>();

		State sqlTransducer = SQLLexer.SQL_TRANSDUCER;
		for (IAbstractString string : strings) {
			string = optimize(string);
			State initial = StringToAutomatonConverter.INSTANCE.convert(string);
			State transduction = AutomataTransduction.INSTANCE.getTransduction(sqlTransducer, initial, SQLLexer.SQL_ALPHABET_CONVERTER);
			transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(transduction);
//			transduction = AutomataDeterminator.determinate(transduction);

			for (Entry<String, IInputToString> entry : expectedMap.entrySet()) {
				IInputToString toStr = entry.getValue();
				Set<String> generated = AutomataTransduction.getSet(generatedMap, entry.getKey());

				List<String> generate = TestUtil.generate(transduction, toStr);
				generated.addAll(generate);
			}
		}
		
		for (Entry<String, Set<String>> entry : generatedMap.entrySet()) {
			String fileName = entry.getKey();
			Set<String> generated = entry.getValue();
			
			
//			FileWriter fileWriter = new FileWriter(fileName);
//			for (String string : generated) {
//				fileWriter.write(string + "\n");
//			}
//			fileWriter.close();
			

			FileReader fileReader = new FileReader(fileName);
			StringBuilder stringBuilder = new StringBuilder();
			int c;
			while ((c = fileReader.read()) != -1) {
				stringBuilder.append((char) c);
			}
			fileReader.close();
			String[] split = stringBuilder.toString().split("\n");
			String[] expected = split.clone();
			Arrays.sort(expected);
			
			String[] genarr = generated.toArray(new String[generated.size()]);
			Arrays.sort(genarr);
		
			assertEquals(expected.length, genarr.length);
			
			for (int i = 0; i < genarr.length; i++) {
				if (!expected[i].equals(genarr[i])) {
					System.out.println(expected[i]);
					System.out.println(genarr[i]);
					System.out.println();
				}
			}
			
			assertEquals(new HashSet<String>(Arrays.asList(expected)), generated);
		}
	}

	@Test
	public void testBig() throws Exception {
		List<IAbstractString> strings = AbstractStringParser.parseFile("data/big.txt");
		IAbstractString big = strings.get(0);
		assertFalse(SyntacticalSQLChecker.hasAcceptableSize(big));
//		System.out.println(AbstractStringSizeCounter.size(big));
//		System.out.println(AbstractStringSizeCounter.size(AbstractStringOptimizer.optimize(big)));
		strings = AbstractStringParser.parseFile("data/new_big.txt");
		IAbstractString big_new = strings.get(0);
		assertFalse(SyntacticalSQLChecker.hasAcceptableSize(big_new));
//		System.out.println(AbstractStringSizeCounter.size(big_new));
//		System.out.println(AbstractStringSizeCounter.size(AbstractStringOptimizer.optimize(big_new)));
//		IAbstractString str = strings.get(0);
//		System.out.println("Parsed. Size: " + size(str));
//		IAbstractString opt = optimize(str);
//		System.out.println("Optimized size: " + size(opt));
//		System.out.println("Optimized size: " + size(optimize(opt)));
//		System.out.println(opt);
//		State initial = FasterStringToAutomatonConverter.INSTANCE.convert(str, SQLLexer.SQL_ALPHABET_CONVERTER);
//		System.out.println(DIFF);
//		AutomataUtils.generate(initial, "", AutomataUtils.ID_MAPPER, AutomataUtils.STANDARD_OUTPUT);
	}
	
//	private static int DIFF = 0;
	
	private static IAbstractString optimize(IAbstractString str) {
		return str.accept(OPTIMIZER, null);
	}
	
	private static final IAbstractStringVisitor<IAbstractString, Void> OPTIMIZER = new IAbstractStringVisitor<IAbstractString, Void>() {
		
		@Override
		public IAbstractString visitStringCharacterSet(
				StringCharacterSet characterSet, Void data) {
			return characterSet;
		}

		@Override
		public IAbstractString visitStringChoice(StringChoice stringChoice,
				Void data) {
			List<IAbstractString> optimized = new ArrayList<IAbstractString>();
			List<StringConstant> constants = new ArrayList<StringConstant>();
			for (IAbstractString item : stringChoice.getItems()) {
				IAbstractString optimizedItem = optimize(item);
				if (optimizedItem instanceof StringChoice) {
					optimized.addAll(((StringChoice) optimizedItem).getItems());
				} else if (optimizedItem instanceof StringConstant) {
					StringConstant constant = (StringConstant) optimizedItem;
					constants.add(constant);
				} else {
					optimized.add(optimizedItem);
				}
			}
			if (constants.isEmpty()) {
				return new StringChoice(optimized);
			}
			String max = constants.get(0).getConstant();
			int maxLen = max.length();
			for (StringConstant c : constants) {
				String string = c.getConstant();
				for (int i = 0; i < maxLen; i++) {
					if (i >= string.length()) {
						maxLen = string.length();
						break;
					}
					if (max.charAt(i) != string.charAt(i)) {
						maxLen = i;
						break;
					}
				}
			}
			maxLen--;
			if (maxLen > 0) {
				List<StringConstant> tails = new ArrayList<StringConstant>();
				for (StringConstant c : constants) {
					String constant = c.getConstant();
					if (constant.length() > maxLen) {
						tails.add(new StringConstant(constant.substring(maxLen)));
					} else {
						throw new IllegalStateException();
					}
				}
				optimized.add(new StringSequence(new StringConstant(max.substring(0, maxLen)), new StringChoice(tails)));
			} else {
				optimized.addAll(constants);
			}
			return new StringChoice(optimized);
		}

		@Override
		public IAbstractString visitStringConstant(StringConstant stringConstant,
				Void data) {
			return stringConstant;
		}

		@Override
		public IAbstractString visitStringRepetition(
				StringRepetition stringRepetition, Void data) {
			IAbstractString optimized = optimize(stringRepetition.getBody());
			if (optimized == stringRepetition.getBody()) {
				return stringRepetition;
			}
			return new StringRepetition(optimized);
		}

		@Override
		public IAbstractString visitStringSequence(StringSequence stringSequence,
				Void data) {
			List<IAbstractString> changed = new ArrayList<IAbstractString>();
			StringBuilder newConstant = new StringBuilder();
			for (IAbstractString item : stringSequence.getItems()) {
				IAbstractString optimized = optimize(item);
				if (optimized instanceof StringConstant) {
					StringConstant con = (StringConstant) optimized;
					newConstant.append(con.getConstant());
				} else {
					if (newConstant.length() > 0) {
						changed.add(new StringConstant(newConstant.toString()));
						newConstant.setLength(0);
					}
					changed.add(optimized);
				}
			}
			if (changed.isEmpty()) {
				return new StringConstant(newConstant.toString());
			}
			if (newConstant.length() > 0) {
				changed.add(new StringConstant(newConstant.toString()));
				newConstant.setLength(0);
			}
			return new StringSequence(changed);
		}

		@Override
		public IAbstractString visitStringParameter(
				StringParameter stringParameter, Void data) {
			throw new IllegalArgumentException();
		}
	};
	
}
