package ee.stacc.productivity.edsl.checkers.sqlstatic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ee.stacc.productivity.edsl.checkers.IAbstractStringChecker;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.sqlparser.IParseErrorHandler;
import ee.stacc.productivity.edsl.sqlparser.ParserSimulator;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.util.AbstractStringSizeCounter;

/**
 * Performs syntax checking for abstract strings containing SQL statements
 * 
 * @author abreslav
 *
 */
public class SyntacticalSQLChecker implements IAbstractStringChecker {

	private static final ILog LOG = Logs.getLog(SyntacticalSQLChecker.class);

	/**
	 * Maximum size of abstract strings. Bigger strings are likely to cause OutOfMemoryError, 
	 * and must be rejected.
	 */
	private static int SIZE_THRESHOLD = 25000;
	
	@Override
	public void checkAbstractStrings(List<IStringNodeDescriptor> descriptors,
			final ISQLErrorHandler errorHandler, Map<String, Object> options) {
		for (final IStringNodeDescriptor descriptor : descriptors) {
			IAbstractString abstractString = descriptor.getAbstractValue();
			if (!hasAcceptableSize(abstractString)) {
				if (abstractString instanceof StringChoice) { // This may make things slower, but more precise 
					StringChoice choice = (StringChoice) abstractString;
					boolean hasBigSubstrings = false;
					boolean hasSmallSubstrings = false;
					for (IAbstractString option : choice.getItems()) {
						if (!hasAcceptableSize(option)) {
							hasBigSubstrings = true;
						} else {
							try {
								checkStringOfAppropriateSize(errorHandler, descriptor, option);
								hasSmallSubstrings = true;
							} catch (StackOverflowError e) { 
								// TODO: This hack is no good. May be it can be fixed in the FixpointParser   
								hasBigSubstrings = true;
							}
						}
					}
					if (hasBigSubstrings) {
						errorHandler.handleSQLWarning("Abstract string is too big" + (hasSmallSubstrings ? ". Only some parts checked" : ""), descriptor.getPosition());
					}
				} else {
					errorHandler.handleSQLWarning("Abstract string is too big", descriptor.getPosition());
				}
			} else {
				try {
					checkStringOfAppropriateSize(errorHandler, descriptor, abstractString);
				} catch (StackOverflowError e) {
					// The analyzer has caused a stack overflow in the dfs-based evaluation procedure.
					// See FixpointParser class
					errorHandler.handleSQLWarning("Abstract string is too big", descriptor.getPosition());
				}
			}
		}
	}

	private void checkStringOfAppropriateSize(
			final ISQLErrorHandler errorHandler,
			final IStringNodeDescriptor descriptor,
			IAbstractString abstractString) {
		try {
			State automaton = PositionedCharacterUtil.createPositionedAutomaton(abstractString);
			
			ParserSimulator.GLR_INSTANCE.checkAutomaton(automaton, new IParseErrorHandler() {
				
				@Override
				public void unexpectedItem(IAbstractInputItem item) {
					Collection<IPosition> markerPositions = PositionedCharacterUtil.getMarkerPositions(((Token) item).getText());
					for (IPosition pos : markerPositions) {
						errorHandler.handleSQLError("Unexpected token: " + PositionedCharacterUtil.render(item), pos);
					}
				}

				@Override
				public void other() {
					errorHandler.handleSQLError("SQL syntax error. Most likely, unfinished query", descriptor.getPosition());
				}

				@Override
				public void overabstraction() {
					errorHandler.handleSQLError("Syntactic analysis failed: nesting is too deep in this sentence", descriptor.getPosition());
				}
			});
		} catch (MalformedStringLiteralException e) {
			IPosition errorPosition = e.getLiteralPosition();
			if (errorPosition == null) {
				errorPosition = descriptor.getPosition(); 
			}
			errorHandler.handleSQLError("Malformed literal: " + e.getMessage(), errorPosition);
		} catch (StackOverflowError e) {  
			// TODO: This hack is no good (see the method above)
			throw e;
		} catch (Throwable e) {
			LOG.exception(e);
			errorHandler.handleSQLError("Static checker internal error: " + e.toString(), descriptor.getPosition());
		}
	}

	/**
	 * Checks if the string is small enough for the corresponding automaton to fit into memory 
	 */
	public static boolean hasAcceptableSize(IAbstractString abstractString) {
		return AbstractStringSizeCounter.size(abstractString) <= SIZE_THRESHOLD;
	}
}
