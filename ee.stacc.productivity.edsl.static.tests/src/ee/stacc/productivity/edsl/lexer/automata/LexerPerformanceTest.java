package ee.stacc.productivity.edsl.lexer.automata;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;
import ee.stacc.productivity.edsl.tests.util.TestUtil;


public class LexerPerformanceTest {

	@Test
	public void testBig() throws Exception {
		final List<IAbstractString> strings = AbstractStringParser.parseFile("data/big.txt");
//		StringToAutomatonConverter.INSTANCE.convert(strings.get(0), AutomataUtils.SQL_ALPHABET_CONVERTER);
	}
	
	@Test
	public void testSqls() throws Exception {
		List<IAbstractString> strings = AbstractStringParser.parseFile("data/sqls.txt");
		
		State sqlTransducer = AutomataConverter.INSTANCE.convert();
		Set<String> generated = new HashSet<String>();
		for (IAbstractString string : strings) {
			State initial = StringToAutomatonConverter.INSTANCE.convert(string, AutomataUtils.SQL_ALPHABET_CONVERTER);
			initial = AutomataDeterminator.determinate(initial);
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
//		
		FileReader fileReader = new FileReader("data/sqls.expected");
		StringBuilder stringBuilder = new StringBuilder();
		int c;
		while ((c = fileReader.read()) != -1) {
			stringBuilder.append((char) c);
		}
		fileReader.close();
		String[] split = stringBuilder.toString().split("\n");
		
		assertEquals(new HashSet<String>(Arrays.asList(split)), generated);
	}
}
