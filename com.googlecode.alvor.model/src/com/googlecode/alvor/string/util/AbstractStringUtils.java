package com.googlecode.alvor.string.util;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.alvor.string.AbstractStringEqualsVisitor;
import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.IAbstractStringVisitor;
import com.googlecode.alvor.string.StringCharacterSet;
import com.googlecode.alvor.string.StringChoice;
import com.googlecode.alvor.string.StringConstant;
import com.googlecode.alvor.string.StringParameter;
import com.googlecode.alvor.string.StringRecursion;
import com.googlecode.alvor.string.StringRepetition;
import com.googlecode.alvor.string.StringSequence;

public class AbstractStringUtils {

	public static final IAbstractStringVisitor<Boolean, Void> LOOP_FINDER = new IAbstractStringVisitor<Boolean, Void>() {
	
		@Override
		public Boolean visitStringCharacterSet(
				StringCharacterSet characterSet, Void data) {
			return false;
		}
	
		@Override
		public Boolean visitStringParameter(
				StringParameter stringParameter, Void data) {
			return false;
		}
	
		@Override
		public Boolean visitStringChoice(StringChoice stringChoice,
				Void data) {
			for (IAbstractString item : stringChoice.getItems()) {
				if (AbstractStringUtils.hasLoops(item)) {
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
				if (AbstractStringUtils.hasLoops(item)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Boolean visitStringRecursion(StringRecursion stringRecursion, Void data) {
			throw new IllegalArgumentException();
		}
	};

	public static boolean hasLoops(IAbstractString str) {
		return str.accept(LOOP_FINDER, null);
	}
	
	public static boolean stringsAreEqual(IAbstractString a, IAbstractString b, boolean ignorePositions) {
		if (ignorePositions) {
			return a.accept(AbstractStringEqualsVisitor.INSTANCE_IGNORE_POS, b);
		}
		else {
			return a.accept(AbstractStringEqualsVisitor.INSTANCE, b);
		}
	}

	public static List<IAbstractString> removeDuplicates(List<IAbstractString> strings, boolean ignorePositions) {
		List<IAbstractString> uniques = new ArrayList<IAbstractString>();
		
		for (IAbstractString string : strings) {
			boolean seenAlready = false;
			for (IAbstractString comparison : uniques) {
				if (stringsAreEqual(string, comparison, ignorePositions)) {
					seenAlready = true;
					break;
				}
			}
			if (!seenAlready) {
				uniques.add(string);
			}
		}
		return uniques;
	}
}
