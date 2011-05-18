package com.googlecode.alvor.sqlparser;

import java.util.List;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;

/**
 * A strategy to handle parse errors
 * 
 * @author abreslav
 *
 */
public interface IParseErrorHandler {

	/**
	 * A default strategy that ignores all errors
	 */
	IParseErrorHandler NONE = new IParseErrorHandler() {

		@Override
		public void other(List<? extends IAbstractInputItem> counterExample) {
		}

		@Override
		public void overabstraction(
				List<? extends IAbstractInputItem> counterExample) {
		}

		@Override
		public void unexpectedItem(IAbstractInputItem item,
				List<? extends IAbstractInputItem> counterExample) {
		}
	};
	
	/**
	 * This method is called if an unexpected token was met
	 */
	void unexpectedItem(IAbstractInputItem item, List<? extends IAbstractInputItem> counterExample);
	
	/**
	 * This method is called if an overabstraction was detected, e.g., a bounded-depth stack reached 
	 * the TRUNCATED element
	 */
	void overabstraction(List<? extends IAbstractInputItem> counterExample);
	
	/**
	 * This method is called in case of all other errors, e.g., unexpected end of input
	 */
	void other(List<? extends IAbstractInputItem> counterExample);
}
