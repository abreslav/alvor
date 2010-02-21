package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.List;
import java.util.Map;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.IPositionDescriptor;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.automata.IInputItemFactory;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.StringToAutomatonConverter;
import ee.stacc.productivity.edsl.sqlparser.IParseErrorHandler;
import ee.stacc.productivity.edsl.sqlparser.SQLSyntaxChecker;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringConstant;

public class SyntacticalSQLChecker implements IAbstractStringChecker {

	@Override
	public void checkAbstractStrings(List<IStringNodeDescriptor> descriptors,
			final ISQLErrorHandler errorHandler, Map<String, Object> options) {
		for (final IStringNodeDescriptor descriptor : descriptors) {
			IAbstractString abstractString = descriptor.getAbstractValue();
			State automaton = StringToAutomatonConverter.INSTANCE.convert(abstractString, new IInputItemFactory() {
				
				@Override
				public IAbstractInputItem createInputItem(StringCharacterSet set,
						int character) {
					throw new IllegalArgumentException("Character sets are not supported");
				}
				
				@Override
				public IAbstractInputItem createInputItem(StringConstant constant,
						int position) {
					return new PositionedCharacter(constant.getConstant().charAt(position), descriptor.getPosition(constant), position);
				}
			});
			
			SQLSyntaxChecker.INSTANCE.checkAutomaton(automaton, new IParseErrorHandler() {
				
				@Override
				public void unexpectedItem(IAbstractInputItem item) {
					errorHandler.handleSQLError("Unexpected " + item, descriptor);
				}
				
				@Override
				public void other() {
					errorHandler.handleSQLError("Unfinished", descriptor);
				}
			});
		}
	}
	
	private static final class PositionedCharacter implements IAbstractInputItem {
		
		private final int code;
		private final IPositionDescriptor stringPosition;
		private final int indexInString;
		
		public PositionedCharacter(int code,
				IPositionDescriptor stringPosition, int indexInString) {
			this.code = code;
			this.stringPosition = stringPosition;
			this.indexInString = indexInString;
		}

		@Override
		public int getCode() {
			return code;
		}

		@Override
		public String toString() {
			return ((char) code) + "[" + stringPosition + ";" + indexInString + "]";
		}
		
	}

}
