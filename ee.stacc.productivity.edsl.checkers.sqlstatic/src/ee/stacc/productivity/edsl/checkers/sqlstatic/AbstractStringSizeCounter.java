/**
 * 
 */
package ee.stacc.productivity.edsl.checkers.sqlstatic;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringParameter;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;

public final class AbstractStringSizeCounter { 

	private static final IAbstractStringVisitor<Integer, Void> COUNTER = new IAbstractStringVisitor<Integer, Void>() {
		private static final int BASE = 1;
	
		@Override
		public Integer visitStringCharacterSet(
				StringCharacterSet characterSet, Void data) {
			return BASE;
		}
	
		@Override
		public Integer visitStringChoice(StringChoice stringChoice,
				Void data) {
			int result = BASE;
			for (IAbstractString item : stringChoice.getItems()) {
				result += size(item);
			}
			return result;
		}
	
		@Override
		public Integer visitStringConstant(StringConstant stringConstant,
				Void data) {
			return BASE + stringConstant.getConstant().length();
		}
	
		@Override
		public Integer visitStringRepetition(
				StringRepetition stringRepetition, Void data) {
			return BASE + size(stringRepetition.getBody());
		}
	
		@Override
		public Integer visitStringSequence(StringSequence stringSequence,
				Void data) {
			int result = BASE;
			for (IAbstractString item : stringSequence.getItems()) {
				result += size(item);
			}
			return result;
		}
	
		@Override
		public Integer visitStringParameter(StringParameter stringParameter,
				Void data) {
			throw new IllegalArgumentException();
		}
	};

	public static int size(IAbstractString s) {
		return s.accept(COUNTER, null);
	}
}