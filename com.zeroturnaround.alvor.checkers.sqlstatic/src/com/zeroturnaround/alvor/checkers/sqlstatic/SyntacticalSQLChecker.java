package com.zeroturnaround.alvor.checkers.sqlstatic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.checkers.ISQLErrorHandler;
import com.zeroturnaround.alvor.checkers.IStringNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;
import com.zeroturnaround.alvor.lexer.alphabet.Token;
import com.zeroturnaround.alvor.lexer.automata.State;
import com.zeroturnaround.alvor.sqlparser.IParseErrorHandler;
import com.zeroturnaround.alvor.sqlparser.ParserSimulator;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.util.AbstractStringSizeCounter;

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
			final ISQLErrorHandler errorHandler, Map<String, String> options) {
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
						errorHandler.handleSQLWarning("SQL syntax checker: Abstract string is too big" + (hasSmallSubstrings ? ". Only some parts checked" : ""), descriptor.getPosition());
					}
				} else {
					errorHandler.handleSQLWarning("SQL syntax checker: Abstract string is too big", descriptor.getPosition());
				}
			} else {
				try {
					checkStringOfAppropriateSize(errorHandler, descriptor, abstractString);
				} catch (StackOverflowError e) {
					// The analyzer has caused a stack overflow in the dfs-based evaluation procedure.
					// See FixpointParser class
					errorHandler.handleSQLWarning("SQL syntax checker: Abstract string is too big", descriptor.getPosition());
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
			
			ParserSimulator.getGLRInstance().checkAutomaton(automaton, new IParseErrorHandler() {
				
				@Override
				public void unexpectedItem(IAbstractInputItem item) {
					Collection<IPosition> markerPositions = PositionedCharacterUtil.getMarkerPositions(((Token) item).getText());
					for (IPosition pos : markerPositions) {
						errorHandler.handleSQLError("SQL syntax checker: Unexpected token: " + PositionedCharacterUtil.render(item), pos);
					}
				}

				@Override
				public void other() {
					errorHandler.handleSQLError("SQL syntax checker: Syntax error. Most likely, unfinished query", descriptor.getPosition());
				}

				@Override
				public void overabstraction() {
					errorHandler.handleSQLError("SQL syntax checker: Syntactic analysis failed: nesting is too deep in this sentence", descriptor.getPosition());
				}
			});
		} catch (MalformedStringLiteralException e) {
			IPosition errorPosition = e.getLiteralPosition();
			if (errorPosition == null) {
				errorPosition = descriptor.getPosition(); 
			}
			errorHandler.handleSQLError("SQL syntax checker: Malformed literal: " + e.getMessage(), errorPosition);
		} catch (StackOverflowError e) {  
			// TODO: This hack is no good (see the method above)
			throw e;
		} catch (Throwable e) {
			LOG.exception(e);
			errorHandler.handleSQLError("SQL syntax checker: internal error: " + e.toString(), descriptor.getPosition());
		}
	}

	/**
	 * Checks if the string is small enough for the corresponding automaton to fit into memory 
	 */
	public static boolean hasAcceptableSize(IAbstractString abstractString) {
		return AbstractStringSizeCounter.size(abstractString) <= SIZE_THRESHOLD;
	}
}
