package com.zeroturnaround.alvor.lexer.alphabet;

import com.zeroturnaround.alvor.lexer.alphabet.ISequence.IFoldFunction;

public class SequenceUtil {

	private static final IFoldFunction<StringBuilder, Object> TO_STRING = new IFoldFunction<StringBuilder, Object>() {

		@Override
		public StringBuilder body(StringBuilder result, Object arg, boolean last) {
			return result.append(arg);
		}
	
	};
	
	/**
	 * Concatenates toString values of all items in the sequence.
	 */
	public static <E> String toString(ISequence<E> seq) {
		return seq.fold(new StringBuilder(), TO_STRING).toString();
	}
	
}
