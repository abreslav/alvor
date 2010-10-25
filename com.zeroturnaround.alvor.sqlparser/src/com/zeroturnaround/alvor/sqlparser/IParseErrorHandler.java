package com.zeroturnaround.alvor.sqlparser;

import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;

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
		public void unexpectedItem(IAbstractInputItem item) {
		}
		
		@Override
		public void other() {
		}
		
		@Override
		public void overabstraction() {
		}
	};
	
	/**
	 * This method is called if an unexpected token was met
	 */
	void unexpectedItem(IAbstractInputItem item);
	
	/**
	 * This method is called if an overabstraction was detected, e.g., a bounded-depth stack reached 
	 * the TRUNCATED element
	 */
	void overabstraction();
	
	/**
	 * This method is called in case of all other errors, e.g., unexpected end of input
	 */
	void other();
}
