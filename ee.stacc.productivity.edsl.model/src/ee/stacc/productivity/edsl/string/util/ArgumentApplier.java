package ee.stacc.productivity.edsl.string.util;

import java.util.ArrayList;
import java.util.List;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;
import ee.stacc.productivity.edsl.string.StringParameter;

public class ArgumentApplier {
	public static final IAbstractStringVisitor<IAbstractString, List<IAbstractString>> 
		ARGUMENT_APPLICATOR = new IAbstractStringVisitor<IAbstractString, List<IAbstractString>>() {
		
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
				items.add(ArgumentApplier.applyArguments(item, data));
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
					ArgumentApplier.applyArguments(stringRepetition.getBody(), data));
		}
	
		@Override
		public IAbstractString visitStringSequence(StringSequence stringSequence,
				List<IAbstractString> data) {
			List<IAbstractString> items = new ArrayList<IAbstractString>();
			
			for (IAbstractString item : stringSequence.getItems()) {
				items.add(ArgumentApplier.applyArguments(item, data));
			}
			
			return new StringSequence(stringSequence.getPosition(), items);
		}
	};

	public static IAbstractString applyArguments(IAbstractString str,
			List<IAbstractString> arguments) {
		return str.accept(ARGUMENT_APPLICATOR, arguments);
	}

}
