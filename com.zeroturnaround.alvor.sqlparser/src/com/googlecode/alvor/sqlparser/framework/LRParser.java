package com.googlecode.alvor.sqlparser.framework;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.lexer.automata.IAlphabetConverter;
import com.googlecode.alvor.sqlparser.ErrorState;
import com.googlecode.alvor.sqlparser.ILRParser;
import com.googlecode.alvor.sqlparser.IParserStackLike;
import com.googlecode.alvor.sqlparser.IStackFactory;

public class LRParser<S extends IParserStackLike> implements IAbstractablePredicate<S, IAbstractInputItem>{

	public static final class UnexpectedTokenError implements IError {

		private final IAbstractInputItem unexpectedItem;
		
		public UnexpectedTokenError(IAbstractInputItem unexpectedItem) {
			this.unexpectedItem = unexpectedItem;
		}

		@Override
		public String toString() {
			return "Unexpected token: " + unexpectedItem.toString();
		}
		
		public IAbstractInputItem getUnexpectedItem() {
			return unexpectedItem;
		}		
	}
	
	public static IError OVERABSTRACTION_ERROR = new IError() {

		@Override
		public String toString() {
			return "Overabstraction: a stack was too deep";
		}
		
	};
	
	public static IError OTHER_ERROR = new IError() {
		
		@Override
		public String toString() {
			return "Syntax error. Most probably, unfinished query.";
		}
		
	};
	
	private final ILRParser<S> lrParser;
	private final IStackFactory<S> stackFactory;
	private final IAlphabetConverter alphabetConverter;

	public LRParser(ILRParser<S> lrParser, IStackFactory<S> stackFactory,
			IAlphabetConverter alphabetConverter) {
		this.lrParser = lrParser;
		this.stackFactory = stackFactory;
		this.alphabetConverter = alphabetConverter;
	}

	@Override
	public S getInitialState() {
		return stackFactory.newStack(lrParser.getInitialState());
	}

	@Override
	public IError getError(S state) {
		if (!state.hasErrorOnTop()) {
			return IError.NO_ERROR;
		}
		ErrorState error = (ErrorState) state.getErrorOnTop();
		IAbstractInputItem unexpectedItem = error.getUnexpectedItem();
		if (unexpectedItem == null) {
			return OVERABSTRACTION_ERROR;
		} 
		if (unexpectedItem.getCode() >= 0) {
			return new UnexpectedTokenError(unexpectedItem);
		} 
		return OTHER_ERROR;
	}

	@Override
	public S transition(S state, IAbstractInputItem character) {
		int tokenIndex = alphabetConverter.convert(character.getCode());
		return lrParser.processToken(character, tokenIndex, state);
	}
	
}
