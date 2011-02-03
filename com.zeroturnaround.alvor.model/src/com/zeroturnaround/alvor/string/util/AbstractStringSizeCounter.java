/**
 * 
 */
package com.zeroturnaround.alvor.string.util;

import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRecursion;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;

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
