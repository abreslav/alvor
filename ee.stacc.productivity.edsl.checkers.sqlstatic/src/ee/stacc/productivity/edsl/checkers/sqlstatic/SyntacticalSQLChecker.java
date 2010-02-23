package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

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
						errorHandler.handleSQLError("Unexpected " + item, descriptor);
					}
					
					@Override
					public void other() {
						errorHandler.handleSQLError("Unfinished", descriptor);
					}
				});
			} catch (MalformedStringLiteralException e) {
				errorHandler.handleSQLError("Malformed string literal: " + e.getMessage(), descriptor);
			}
		}
	}
	
	private static Collection<IPositionDescriptor> getMarkerPositions(List<PositionedCharacter> chars) {
		if (chars.isEmpty()) {
			return Collections.emptySet();
		}
		
		Collection<IPositionDescriptor> positions = new ArrayList<IPositionDescriptor>();
		
		
		Iterator<PositionedCharacter> iterator = chars.iterator();
		PositionedCharacter currentChar = iterator.next();
		IFile currentFile = currentChar.getStringPosition().getFile();
		int charStart = currentChar.getStringPosition().getCharStart() + currentChar.getIndexInString();
		int charLength = 1;
		
		while (iterator.hasNext()) {
//			rough edge
		}
		
		return positions;
	}
	
}
