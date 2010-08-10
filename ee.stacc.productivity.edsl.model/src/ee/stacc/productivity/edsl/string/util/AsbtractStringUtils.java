package ee.stacc.productivity.edsl.string.util;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;
import ee.stacc.productivity.edsl.string.StringParameter;

public class AsbtractStringUtils {

	public static final IAbstractStringVisitor<Boolean, Void> LOOP_FINDER = new IAbstractStringVisitor<Boolean, Void>() {
	
		@Override
		public Boolean visitStringCharacterSet(
				StringCharacterSet characterSet, Void data) {
			return false;
		}
	
		@Override
		public Boolean visitStringParameter(
				StringParameter stringParameter, Void data) {
			throw new IllegalArgumentException();
		}
	
		@Override
		public Boolean visitStringChoice(StringChoice stringChoice,
				Void data) {
			for (IAbstractString item : stringChoice.getItems()) {
				if (AsbtractStringUtils.hasLoops(item)) {
					return true;
				}
			}
			return false;
		}
	
		@Override
		public Boolean visitStringConstant(StringConstant stringConstant,
				Void data) {
			return false;
		}
	
		@Override
		public Boolean visitStringRepetition(
				StringRepetition stringRepetition, Void data) {
			return true;
		}
	
		@Override
		public Boolean visitStringSequence(StringSequence stringSequence,
				Void data) {
			for (IAbstractString item : stringSequence.getItems()) {
				if (AsbtractStringUtils.hasLoops(item)) {
					return true;
				}
			}
			return false;
		}
	};

	public static boolean hasLoops(IAbstractString str) {
		return str.accept(LOOP_FINDER, null);
	}

}
