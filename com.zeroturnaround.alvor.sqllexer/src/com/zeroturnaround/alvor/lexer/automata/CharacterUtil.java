package com.zeroturnaround.alvor.lexer.automata;

import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;
import com.zeroturnaround.alvor.lexer.alphabet.ISequence;
import com.zeroturnaround.alvor.lexer.alphabet.ISequence.IFoldFunction;

public class CharacterUtil {

	public static final IFoldFunction<StringBuilder, IAbstractInputItem> TOKEN_TO_STRING = new IFoldFunction<StringBuilder, IAbstractInputItem>() {
		
		@Override
		public StringBuilder body(StringBuilder result, IAbstractInputItem arg,
				boolean last) {
			return result.append((char) arg.getCode());
		}
	};

	public static String toString(ISequence<IAbstractInputItem> result) {
		return result.fold(new StringBuilder(), TOKEN_TO_STRING).toString();
	}

}
