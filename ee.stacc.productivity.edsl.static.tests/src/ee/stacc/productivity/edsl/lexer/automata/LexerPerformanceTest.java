package ee.stacc.productivity.edsl.lexer.automata;

import java.util.List;

import org.junit.Test;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.parser.AbstractStringParser;


public class LexerPerformanceTest {

	@Test
	public void testBig() throws Exception {
		List<IAbstractString> strings = AbstractStringParser.parseFile("data/big.txt");
		StringToAutomatonConverter.INSTANCE.convert(strings.get(0), AutomataUtils.SQL_ALPHABET_CONVERTER);
		
	}
	
	@Test
	public void testSqls() throws Exception {
		List<IAbstractString> strings = AbstractStringParser.parseFile("data/sqls.txt");
		
		State sqlTransducer = AutomataConverter.INSTANCE.convert();
		
		for (IAbstractString string : strings) {
			System.out.println(string);
			State initial = StringToAutomatonConverter.INSTANCE.convert(string, AutomataUtils.SQL_ALPHABET_CONVERTER);
			initial = AutomataDeterminator.determinate(initial);
			State transduction = AutomataInclusion.INSTANCE.getTrasduction(sqlTransducer, initial);
			transduction = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(transduction);
			transduction = AutomataDeterminator.determinate(transduction);
		}
	}
}
