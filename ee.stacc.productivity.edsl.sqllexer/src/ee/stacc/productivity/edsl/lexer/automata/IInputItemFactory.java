package ee.stacc.productivity.edsl.lexer.automata;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringConstant;

public interface IInputItemFactory {

	IAbstractInputItem[] createInputItems(StringConstant constant);
	IAbstractInputItem createInputItem(StringCharacterSet set, int character);
}
