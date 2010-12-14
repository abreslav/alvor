package com.zeroturnaround.alvor.sqlparser.framework;

import java.util.List;

import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;
import com.zeroturnaround.alvor.lexer.automata.State;

public interface IAbstractInterpreter<S> {

	IError interpret(State inputInitalState,
			List<IAbstractInputItem> counterExample);

}