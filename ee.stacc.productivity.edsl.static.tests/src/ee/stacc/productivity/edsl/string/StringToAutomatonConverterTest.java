package ee.stacc.productivity.edsl.string;

import org.junit.Test;

import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.StringToAutomatonConverter;
import ee.stacc.productivity.edsl.tests.util.TestUtil;


public class StringToAutomatonConverterTest {

	@Test
	public void test() throws Exception {
		StringConstant stringConstant = new StringConstant("\"asdas");
		StringCharacterSet stringCharacterSet = new StringCharacterSet("sadf]][]");
		StringConstant subStr = new StringConstant("sa");
		StringRepetition stringRepetition = new StringRepetition(subStr);
		StringChoice stringChoice = new StringChoice(
				stringCharacterSet,
				stringRepetition
		);
		StringSequence stringSequence = new StringSequence(
				stringConstant,
				stringChoice
		);
		
		State initial;
		
		initial = StringToAutomatonConverter.INSTANCE.convert(stringConstant);
		TestUtil.checkGeneratedcharacterStrings(initial, "\"asdas");

		initial = StringToAutomatonConverter.INSTANCE.convert(stringCharacterSet);
		TestUtil.checkGeneratedcharacterStrings(initial, "s", "a", "d", "f", "]", "[");
		
		initial = StringToAutomatonConverter.INSTANCE.convert(stringRepetition);
		TestUtil.checkGeneratedcharacterStrings(initial, "sa");
		
		initial = StringToAutomatonConverter.INSTANCE.convert(stringChoice);
		TestUtil.checkGeneratedcharacterStrings(initial, "sasa", "sa", "s", "a", "d", "f", "]", "[");
		
		initial = StringToAutomatonConverter.INSTANCE.convert(stringSequence);
		TestUtil.checkGeneratedcharacterStrings(initial, 
				"\"asdassasa", 
				"\"asdassa", 
				"\"asdass", 
				"\"asdasa", 
				"\"asdasd", 
				"\"asdasf", 
				"\"asdas]", 
				"\"asdas[");
		
		initial = StringToAutomatonConverter.INSTANCE.convert(new StringRepetition(new StringCharacterSet("a1")));
		TestUtil.checkGeneratedcharacterStrings(initial, 
				"1 1 a  ",
				"1 1  ",
				"1 a 1  ",
				"1 a  ",
				"1  ",
				"a 1 a  ",
				"a 1  ",
				"a a 1  ",
				"a a  ",
				"a  ");
	}
}
