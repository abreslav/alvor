/**
 * 
 */
package com.googlecode.alvor.string.util;

import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.IAbstractStringVisitor;
import com.googlecode.alvor.string.StringCharacterSet;
import com.googlecode.alvor.string.StringChoice;
import com.googlecode.alvor.string.StringConstant;
import com.googlecode.alvor.string.StringParameter;
import com.googlecode.alvor.string.StringRecursion;
import com.googlecode.alvor.string.StringRepetition;
import com.googlecode.alvor.string.StringSequence;

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
			return BASE;
		}

		@Override
		public Integer visitStringRecursion(StringRecursion stringRecursion, Void data) {
			return BASE;
		}
	};

	public static int size(IAbstractString s) {
		return s.accept(COUNTER, null);
	}
}
