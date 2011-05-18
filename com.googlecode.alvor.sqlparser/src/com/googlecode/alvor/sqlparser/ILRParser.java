package com.googlecode.alvor.sqlparser;

import java.util.Map;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;

/**
 * Represents an LRParser which can process tokens and modify state stacks according to its tables 
 * 
 * @author abreslav
 *
 * @param <S> a type of stacks used by this parser
 */
public interface ILRParser<S> {
	/**
	 * Transforms a state stack until the given token is consumed or accepting parsing state 
	 * is reached or a parsing error state is reached.
	 * 
	 * @param token current token (input item), indexed in terms of the lexer output
	 * @param tokenIndex a token index in terms of the input alphabet of this parser
	 * @param stack current state stack
	 * @return a new stack representing the state after processing the token
	 */
	S processToken(IAbstractInputItem token, int tokenIndex, S stack);

	/**
	 * @return the initial parsing state object for this parser 
	 */
	IParserState getInitialState();

	/**
	 * @return the index of the EOF (end of file) token in the input alphabet of this parser
	 */
	int getEofTokenIndex();
	
	/**
	 * @return a map from token names to their numbers (indices) in the input alphabet of this parser
	 */
	Map<String, Integer> getNamesToTokenNumbers();

	/**
	 * @return a map from parser's input alphabet codes to symbol names
	 */
	Map<Integer, String> getSymbolNumbersToNames();
}
