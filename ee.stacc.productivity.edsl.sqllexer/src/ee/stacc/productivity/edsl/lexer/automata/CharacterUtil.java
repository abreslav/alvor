package ee.stacc.productivity.edsl.lexer.automata;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence.IFoldFunction;

public class CharacterUtil {

	public static final IFoldFunction<StringBuilder, IAbstractInputItem> TOKEN_TO_STRING = new IFoldFunction<StringBuilder, IAbstractInputItem>() {
		
		@Override
		public StringBuilder body(StringBuilder init, IAbstractInputItem arg,
				boolean last) {
			return init.append((char) arg.getCode());
		}
	};

	public static String toString(ISequence<IAbstractInputItem> result) {
		return result.fold(new StringBuilder(), TOKEN_TO_STRING).toString();
	}

}
