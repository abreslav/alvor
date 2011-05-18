package com.googlecode.alvor.sqlparser.framework;

import java.util.List;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.lexer.automata.State;

public interface IAbstractInterpreter<S> {

	IError interpret(State inputInitalState,
			List<IAbstractInputItem> counterExample);

}