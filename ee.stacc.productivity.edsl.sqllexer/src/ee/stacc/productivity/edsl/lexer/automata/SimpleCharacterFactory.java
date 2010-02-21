package ee.stacc.productivity.edsl.lexer.automata;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.SimpleCharacter;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringConstant;

public class SimpleCharacterFactory implements IInputItemFactory {

	public static final SimpleCharacterFactory INSTANCE = new SimpleCharacterFactory();
	
	private SimpleCharacterFactory() {}
	
	@Override
	public IAbstractInputItem createInputItem(StringConstant constant,
			int position) {
		return SimpleCharacter.create(constant.getConstant().charAt(position));
	}

	@Override
	public IAbstractInputItem createInputItem(StringCharacterSet set,
			int character) {
		return SimpleCharacter.create(character);
	}

}
