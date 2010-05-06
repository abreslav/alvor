package ee.stacc.productivity.edsl.lexer.alphabet;

import ee.stacc.productivity.edsl.lexer.alphabet.ISequence.IFoldFunction;

public class SequenceUtil {

	private static final IFoldFunction<StringBuilder, Object> TO_STRING = new IFoldFunction<StringBuilder, Object>() {

		@Override
		public StringBuilder body(StringBuilder init, Object arg, boolean last) {
			return init.append(arg);
		}
	
	};
	
	public static <E> String toString(ISequence<E> seq) {
		return seq.fold(new StringBuilder(), TO_STRING).toString();
	}
	
}
