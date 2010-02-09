package ee.stacc.productivity.edsl.lexer.automata;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.lexer.sql.SQLLexer;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;
import ee.stacc.productivity.edsl.tests.util.TestUtil;


public class LexerPerformanceTest {
	@Test
	public void testShorts() throws Exception {
		List<IAbstractString> strings = AbstractStringParser.parseFile("data/sqls.txt");
		
		State sqlTransducer = SQLLexer.SQL_TRANSDUCER;
		Set<String> generated = new HashSet<String>();
		for (IAbstractString string : strings) {
			string = optimize(string);
			State initial = StringToAutomatonConverter.INSTANCE.convert(string, SQLLexer.SQL_ALPHABET_CONVERTER);
			State transduction = AutomataInclusion.INSTANCE.getTrasduction(sqlTransducer, initial);
			transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(transduction);
			transduction = AutomataDeterminator.determinate(transduction);

			List<String> generate = TestUtil.generate(transduction, "", AutomataUtils.SQL_TOKEN_MAPPER);
			generated.addAll(generate);
		}
		
//		FileWriter fileWriter = new FileWriter("data/sqls.expected");
//		for (String string : generated) {
//			fileWriter.write(string + "\n");
//		}
//		fileWriter.close();

		FileReader fileReader = new FileReader("data/sqls.expected");
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

	@Test
	public void testBig() throws Exception {
		final List<IAbstractString> strings = AbstractStringParser.parseFile("data/big.txt");
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
	
	private static int size(IAbstractString s) {
		return s.accept(SIZE_COUNTER, null);
	}
	
	private static final int BASE = 1;
	private static final IAbstractStringVisitor<Integer, Void> SIZE_COUNTER = new IAbstractStringVisitor<Integer, Void>() {
		
		@Override
		public Integer visitStringCharacterSet(
				StringCharacterSet characterSet, Void data) {
			return BASE;
		}

		@Override
		public Integer visitStringChoise(StringChoice stringChoise,
				Void data) {
			int result = BASE;
			for (IAbstractString item : stringChoise.getItems()) {
				result += size(item);
			}
			return result;
		}

		@Override
		public Integer visitStringConstant(StringConstant stringConstant,
				Void data) {
			return BASE + stringConstant.getConstant().length();
		}

		@Override
		public Integer visitStringRepetition(
				StringRepetition stringRepetition, Void data) {
			return BASE + size(stringRepetition.getBody());
		}

		@Override
		public Integer visitStringSequence(StringSequence stringSequence,
				Void data) {
			int result = BASE;
			for (IAbstractString item : stringSequence.getItems()) {
				result += size(item);
			}
			return result;
		}
	};

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
		public IAbstractString visitStringChoise(StringChoice stringChoise,
				Void data) {
			List<IAbstractString> optimized = new ArrayList<IAbstractString>();
			List<StringConstant> constants = new ArrayList<StringConstant>();
			for (IAbstractString item : stringChoise.getItems()) {
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
				if (newConstant != null && optimized instanceof StringConstant) {
					StringConstant con = (StringConstant) optimized;
					newConstant.append(con.getConstant());
				} else {
					if (newConstant != null && newConstant.length() > 0) {
						changed.add(new StringConstant(newConstant.toString()));
						newConstant.setLength(0);
					}
					changed.add(optimized);
				}
			}
			if (changed.isEmpty()) {
				return new StringConstant(newConstant.toString());
			}
			if (newConstant != null && newConstant.length() > 0) {
				changed.add(new StringConstant(newConstant.toString()));
				newConstant.setLength(0);
			}
			return new StringSequence(changed);
		}
	};
	
}