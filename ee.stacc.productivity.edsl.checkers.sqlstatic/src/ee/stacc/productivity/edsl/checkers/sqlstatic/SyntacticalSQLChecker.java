package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.IPositionDescriptor;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.checkers.PositionDescriptor;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence.IFoldFunction;
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
			try {
				State automaton = StringToAutomatonConverter.INSTANCE.convert(abstractString, new IInputItemFactory() {
					
					@Override
					public IAbstractInputItem createInputItem(StringCharacterSet set,
							int character) {
						throw new IllegalArgumentException("Character sets are not supported");
					}
					
					@Override
					public IAbstractInputItem[] createInputItems(StringConstant constant) {
						IAbstractInputItem[] result = new IAbstractInputItem[constant.getConstant().length()];
	
						String escapedValue = descriptor.getEscapedValue(constant);
	
						System.out.println(escapedValue);
						JavaStringLexer.tokenizeJavaString(escapedValue, result, descriptor.getPosition(constant));
						return result;
					}
	
				});
				
				SQLSyntaxChecker.INSTANCE.checkAutomaton(automaton, new IParseErrorHandler() {
					
					@Override
					public void unexpectedItem(IAbstractInputItem item) {
						Collection<IPositionDescriptor> markerPositions = getMarkerPositions(((Token) item).getText());
						for (IPositionDescriptor pos : markerPositions) {
							errorHandler.handleSQLError("Unexpected " + item, pos);
						}
					}
					
					@Override
					public void other() {
						errorHandler.handleSQLError("Unfinished", descriptor);
					}
				});
			} catch (MalformedStringLiteralException e) {
				errorHandler.handleSQLError("Malformed literal: " + e.getMessage(), descriptor);
			}
		}
	}
	
	private static Collection<IPositionDescriptor> getMarkerPositions(ISequence<IAbstractInputItem> text) {
		if (text.isEmpty()) {
			return Collections.emptySet();
		}
		
		List<PositionedCharacter> chars = text.fold(new ArrayList<PositionedCharacter>(), new IFoldFunction<List<PositionedCharacter>, IAbstractInputItem>() {
			@Override
			public List<PositionedCharacter> body(
					List<PositionedCharacter> init, IAbstractInputItem arg,
					boolean last) {
				init.add((PositionedCharacter) arg);
				return init;
			}
		});
		
		Collection<IPositionDescriptor> positions = new ArrayList<IPositionDescriptor>();
		
		int charLength = -1;
		int charStart = -1;
		IPositionDescriptor currentStringPosition = null;
		
		for (PositionedCharacter currentChar : chars) {
			IPositionDescriptor stringPosition = currentChar.getStringPosition();
			if (stringPosition == currentStringPosition) {
				charLength += currentChar.getLengthInSource();
			} else {
				addTokenPositionDescriptor(positions, charStart, charLength,
						currentStringPosition);
				currentStringPosition = stringPosition;
				charLength = currentChar.getLengthInSource();
				charStart = currentChar.getIndexInString();
			}
		}
		addTokenPositionDescriptor(positions, charStart, charLength,
				currentStringPosition);
		
		return positions;
	}

	private static void addTokenPositionDescriptor(
			Collection<IPositionDescriptor> positions, int charStart,
			int charLength, IPositionDescriptor stringPosition) {
		if (stringPosition != null) {
			positions.add(new PositionDescriptor(
					stringPosition.getFile(), 
					stringPosition.getCharStart() + charStart, 
					charLength));
		}
	}
	
}
