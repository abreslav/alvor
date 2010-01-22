package ee.stacc.productivity.edsl.string;

import org.junit.Test;

import ee.stacc.productivity.edsl.lexer.automata.AutomataUtils;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.StringToAutomatonConverter;


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
		AutomataUtils.printAutomaton(initial);

		initial = StringToAutomatonConverter.INSTANCE.convert(stringCharacterSet);
		AutomataUtils.printAutomaton(initial);
		
		initial = StringToAutomatonConverter.INSTANCE.convert(stringRepetition);
		AutomataUtils.printAutomaton(initial);
		
		initial = StringToAutomatonConverter.INSTANCE.convert(stringChoice);
		AutomataUtils.printAutomaton(initial);
		AutomataUtils.generate(initial, "", AutomataUtils.ID_MAPPER);
		
		initial = StringToAutomatonConverter.INSTANCE.convert(stringSequence);
		AutomataUtils.printAutomaton(initial);
		AutomataUtils.generate(initial, "", AutomataUtils.ID_MAPPER);
		
		initial = StringToAutomatonConverter.INSTANCE.convert(stringRepetition);
		AutomataUtils.printAutomaton(initial);
		AutomataUtils.generate(initial, "", AutomataUtils.ID_MAPPER);
		
		initial = StringToAutomatonConverter.INSTANCE.convert(new StringRepetition(new StringCharacterSet("a1")));
		AutomataUtils.printAutomaton(initial);
		AutomataUtils.generate(initial, "", AutomataUtils.ID_MAPPER);
	}
}
