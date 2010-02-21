package ee.stacc.productivity.edsl.lexer.automata;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public abstract class AbstractCharacterMapper implements ICharacterMapper {

	@Override
	public String toString(IAbstractInputItem item) {
		return map(item.getCode());
	}

}
