package ee.stacc.productivity.edsl.lexer.automata;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.SimpleCharacter;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringConstant;

public class SimpleCharacterFactory implements IInputItemFactory {

	public static final SimpleCharacterFactory INSTANCE = new SimpleCharacterFactory();
	
	private SimpleCharacterFactory() {}
	
	@Override
	public IAbstractInputItem[] createInputItems(StringConstant constant) {
		String string = constant.getConstant();
		int length = string.length();
		IAbstractInputItem[] result = new IAbstractInputItem[length];
		for (int i = 0; i < length; i++) {
			result[i] = SimpleCharacter.create(constant.getConstant().charAt(i));
		}
		return result;
	}

	@Override
	public IAbstractInputItem createInputItem(StringCharacterSet set,
			int character) {
		return SimpleCharacter.create(character);
	}

}
