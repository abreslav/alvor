package com.zeroturnaround.alvor.lexer.automata;

import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;

public abstract class AbstractCharacterMapper implements ICharacterMapper {

	@Override
	public String toString(IAbstractInputItem item) {
		return map(item.getCode());
	}

}
