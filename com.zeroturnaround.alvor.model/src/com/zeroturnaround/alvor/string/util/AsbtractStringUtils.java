package com.zeroturnaround.alvor.string.util;

import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;
import com.zeroturnaround.alvor.string.StringParameter;

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
