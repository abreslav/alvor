package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
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
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.Position;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringConstant;

public class SyntacticalSQLChecker implements IAbstractStringChecker {

	private static int SIZE_THRESHOLD = 25000;
	
	@Override
	public void checkAbstractStrings(List<IStringNodeDescriptor> descriptors,
			final ISQLErrorHandler errorHandler, Map<String, Object> options) {
		for (final IStringNodeDescriptor descriptor : descriptors) {
			IAbstractString abstractString = descriptor.getAbstractValue();
			if (!hasAcceptableSize(abstractString)) {
				errorHandler.handleSQLWarning("Abstract string is too big", abstractString.getPosition());
				continue;
			}
			try {
				State automaton = StringToAutomatonConverter.INSTANCE.convert(abstractString, new IInputItemFactory() {
					
					@Override
					public IAbstractInputItem createInputItem(StringCharacterSet set,
							int character) {
						IPosition position = set.getPosition();
						return new PositionedCharacter(character, position, 0, position.getLength());
					}
					
					@Override
					public IAbstractInputItem[] createInputItems(StringConstant constant) {
						IAbstractInputItem[] result = new IAbstractInputItem[constant.getConstant().length()];
	
						String escapedValue = constant.getEscapedValue();
	
						JavaStringLexer.tokenizeJavaString(escapedValue, result, constant.getPosition());
						return result;
					}
	
				});
				
				SQLSyntaxChecker.INSTANCE.checkAutomaton(automaton, new IParseErrorHandler() {
					
					@Override
					public void unexpectedItem(IAbstractInputItem item) {
						Collection<IPosition> markerPositions = getMarkerPositions(((Token) item).getText());
						for (IPosition pos : markerPositions) {
							errorHandler.handleSQLError("Unexpected token: " + render(item), pos);
						}
					}

					@Override
					public void other() {
						errorHandler.handleSQLError("SQL syntax error. Most likely, unfinished query", descriptor.getPosition());
					}
				});
			} catch (MalformedStringLiteralException e) {
				errorHandler.handleSQLError("Malformed literal: " + e.getMessage(), descriptor.getPosition());
			} catch (Throwable e) {
				errorHandler.handleSQLError("Internal error: " + e.toString(), abstractString.getPosition());
			}
		}
	}
	
	public static boolean hasAcceptableSize(IAbstractString abstractString) {
		return AbstractStringSizeCounter.size(abstractString) <= SIZE_THRESHOLD;
	}

	private static Collection<IPosition> getMarkerPositions(ISequence<IAbstractInputItem> text) {
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
		
		Collection<IPosition> positions = new ArrayList<IPosition>();
		
		int charLength = -1;
		int charStart = -1;
		IPosition currentStringPosition = null;
		
		for (PositionedCharacter currentChar : chars) {
			IPosition stringPosition = currentChar.getStringPosition();
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
			Collection<IPosition> positions, int charStart,
			int charLength, IPosition stringPosition) {
		if (stringPosition != null) {
			positions.add(new Position(
					stringPosition.getPath(), 
					stringPosition.getStart() + charStart, 
					charLength));
		}
	}
	
	
	private String render(IAbstractInputItem item) {
		if (item instanceof Token) {
			Token token = (Token) item;
			StringBuilder text = token.getText().fold(new StringBuilder(), new IFoldFunction<StringBuilder, IAbstractInputItem>() {
				
				@Override
				public StringBuilder body(StringBuilder init, IAbstractInputItem arg,
						boolean last) {
					return init.append((char) arg.getCode());
				}
			});
			return text.toString();
		}
		return item.toString();
	}
}
