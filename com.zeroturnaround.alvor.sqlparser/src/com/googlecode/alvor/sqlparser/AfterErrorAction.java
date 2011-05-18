/**
 * 
 */
package com.googlecode.alvor.sqlparser;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;

/**
 * An action available in the ERROR state.
 * 
 * @author abreslav
 *
 */
public final class AfterErrorAction extends AbstractAction {
	@Override
	public IParserStack process(IAbstractInputItem inputItem,
			IParserStack stack) {
		return stack;
	}

	@Override
	public boolean isError() {
		return true;
	}
}
