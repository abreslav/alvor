package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence.IFoldFunction;
import ee.stacc.productivity.edsl.lexer.automata.CharacterUtil;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.StringToAutomatonConverter;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.Position;

/**
 * Implements miscellaneous functions on positioned characters
 * 
 * @author abreslav
 *
 */
public class PositionedCharacterUtil {

	/**
	 * Converts an abstract strings into an automaton with positioned characters on edges
	 */
	public static State createPositionedAutomaton(IAbstractString abstractString) {
		return StringToAutomatonConverter.INSTANCE.convert(abstractString, PositionedCharacter.FACTORY);
	}

	/**
	 * Returns a list of positions to be marked in a file in order to underline all characters in the given text.
	 * Why not only one position? Because one token can be spread over several string literals located on different 
	 * lines and maybe even in different files.
	 */
	public static Collection<IPosition> getMarkerPositions(ISequence<IAbstractInputItem> text) {
		if (text.isEmpty()) {
			return Collections.emptySet();
		}
		
		List<PositionedCharacter> chars = text.fold(new ArrayList<PositionedCharacter>(), new IFoldFunction<List<PositionedCharacter>, IAbstractInputItem>() {
			@Override
			public List<PositionedCharacter> body(
					List<PositionedCharacter> result, IAbstractInputItem arg,
					boolean last) {
				result.add((PositionedCharacter) arg);
				return result;
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

	/**
	 * Renders tokens as their texts
	 */
	public static String render(IAbstractInputItem item) {
		if (item instanceof Token) {
			Token token = (Token) item;
			return CharacterUtil.toString(token.getText());
		}
		return item.toString();
	}
	
	
}
