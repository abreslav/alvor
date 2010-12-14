package com.zeroturnaround.alvor.sqlparser.framework;

import com.zeroturnaround.alvor.lexer.automata.Transition;

public interface ITransitionLength {
	ITransitionLength ONE = new ITransitionLength() {

		@Override
		public int length(Transition transition) {
			return 1;
		}
		
	};
	
	int length(Transition transition);
}
