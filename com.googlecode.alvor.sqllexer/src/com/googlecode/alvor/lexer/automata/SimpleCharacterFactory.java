package com.googlecode.alvor.lexer.automata;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.lexer.alphabet.SimpleCharacter;
import com.googlecode.alvor.string.StringCharacterSet;
import com.googlecode.alvor.string.StringConstant;

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
