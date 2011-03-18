package com.zeroturnaround.alvor.string.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringRecursion;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;
import com.zeroturnaround.alvor.string.StringParameter;

public class ArgumentApplier {
	public static final IAbstractStringVisitor<IAbstractString, Map<Integer, IAbstractString>> 
		ARGUMENT_MAP_APPLICATOR = new IAbstractStringVisitor<IAbstractString, Map<Integer, IAbstractString>>() {
		
		@Override
		public IAbstractString visitStringParameter(
				StringParameter stringParameter, Map<Integer, IAbstractString> data) {
			IAbstractString argValue = data.get(stringParameter.getIndex());
			
			if (argValue == null) {
				throw new IllegalArgumentException("Argument " + stringParameter.getIndex() + " not found");
			}
			
			return argValue;
		}
	
		@Override
		public IAbstractString visitStringCharacterSet(
				StringCharacterSet characterSet, Map<Integer, IAbstractString> data) {
			return characterSet;
		}
	
		@Override
		public IAbstractString visitStringChoice(StringChoice stringChoice,
				Map<Integer, IAbstractString> data) {
			
			List<IAbstractString> items = new ArrayList<IAbstractString>();
			
			for (IAbstractString item : stringChoice.getItems()) {
				items.add(ArgumentApplier.applyArgumentsMap(item, data));
			}
			
			return new StringChoice(stringChoice.getPosition(), items);
		}
		
		
	
		@Override
		public IAbstractString visitStringConstant(StringConstant stringConstant,
				Map<Integer, IAbstractString> data) {
			return stringConstant;
		}
	
		@Override
		public IAbstractString visitStringRepetition(
				StringRepetition stringRepetition, Map<Integer, IAbstractString> data) {
			return new StringRepetition(stringRepetition.getPosition(), 
					ArgumentApplier.applyArgumentsMap(stringRepetition.getBody(), data));
		}
	
		@Override
		public IAbstractString visitStringSequence(StringSequence stringSequence,
				Map<Integer, IAbstractString> data) {
			List<IAbstractString> items = new ArrayList<IAbstractString>();
			
			for (IAbstractString item : stringSequence.getItems()) {
				items.add(ArgumentApplier.applyArgumentsMap(item, data));
			}
			
			return new StringSequence(stringSequence.getPosition(), items);
		}

		@Override
		public IAbstractString visitStringRecursion(
				StringRecursion stringRecursion, Map<Integer, IAbstractString> data) {
			return stringRecursion;
		}
	};

	@Deprecated
	public static final IAbstractStringVisitor<IAbstractString, List<IAbstractString>> 
		ARGUMENT_LIST_APPLICATOR = new IAbstractStringVisitor<IAbstractString, List<IAbstractString>>() {
		
		@Override
		public IAbstractString visitStringParameter(
				StringParameter stringParameter, List<IAbstractString> data) {
			return data.get(stringParameter.getIndex());
		}
	
		@Override
		public IAbstractString visitStringCharacterSet(
				StringCharacterSet characterSet, List<IAbstractString> data) {
			return characterSet;
		}
	
		@Override
		public IAbstractString visitStringChoice(StringChoice stringChoice,
				List<IAbstractString> data) {
			
			List<IAbstractString> items = new ArrayList<IAbstractString>();
			
			for (IAbstractString item : stringChoice.getItems()) {
				items.add(ArgumentApplier.applyArgumentsList(item, data));
			}
			
			return new StringChoice(stringChoice.getPosition(), items);
		}
		
		
	
		@Override
		public IAbstractString visitStringConstant(StringConstant stringConstant,
				List<IAbstractString> data) {
			return stringConstant;
		}
	
		@Override
		public IAbstractString visitStringRepetition(
				StringRepetition stringRepetition, List<IAbstractString> data) {
			return new StringRepetition(stringRepetition.getPosition(), 
					ArgumentApplier.applyArgumentsList(stringRepetition.getBody(), data));
		}
	
		@Override
		public IAbstractString visitStringSequence(StringSequence stringSequence,
				List<IAbstractString> data) {
			List<IAbstractString> items = new ArrayList<IAbstractString>();
			
			for (IAbstractString item : stringSequence.getItems()) {
				items.add(ArgumentApplier.applyArgumentsList(item, data));
			}
			
			return new StringSequence(stringSequence.getPosition(), items);
		}

		@Override
		public IAbstractString visitStringRecursion(
				StringRecursion stringRecursion, List<IAbstractString> data) {
			return stringRecursion;
		}
	};

	@Deprecated // used by old crawler
	public static IAbstractString applyArgumentsList(IAbstractString str,
			List<IAbstractString> arguments) {
		return str.accept(ARGUMENT_LIST_APPLICATOR, arguments);
	}

	public static IAbstractString applyArgumentsMap(IAbstractString str,
			Map<Integer, IAbstractString> arguments) {
		return str.accept(ARGUMENT_MAP_APPLICATOR, arguments);
	}

}
