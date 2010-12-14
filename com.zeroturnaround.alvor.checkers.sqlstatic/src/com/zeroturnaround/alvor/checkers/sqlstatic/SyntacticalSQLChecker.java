package com.zeroturnaround.alvor.checkers.sqlstatic;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zeroturnaround.alvor.checkers.CheckerException;
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
			final ISQLErrorHandler errorHandler, Map<String, String> options) throws CheckerException {
		for (final IStringNodeDescriptor descriptor : descriptors) {
			checkAbstractString(descriptor, errorHandler, options);
		}
	}

	private boolean checkStringOfAppropriateSize(
			final ISQLErrorHandler errorHandler,
			final IStringNodeDescriptor descriptor,
			IAbstractString abstractString) throws CheckerException {
		
		// need something mutable to get information from closures
		final Set<Boolean> results = new HashSet<Boolean>(); 
		
		try {
			State automaton = PositionedCharacterUtil.createPositionedAutomaton(abstractString);
			
			ParserSimulator.getGLRInstance().checkAutomaton(automaton, new IParseErrorHandler() {
				
				@Override
				public void unexpectedItem(IAbstractInputItem item,
						List<? extends IAbstractInputItem> counterExampleList) {
					String counterExample = PositionedCharacterUtil.renderCounterExample(counterExampleList);
					Collection<IPosition> markerPositions = PositionedCharacterUtil.getMarkerPositions(((Token) item).getText());
					for (IPosition pos : markerPositions) {
						errorHandler.handleSQLError(
								"SQL syntax checker: Unexpected token: " + PositionedCharacterUtil.render(item) + "\n" + 
								"Counter example: " + counterExample, 
								pos);
						results.add(false);
					}
				}

				@Override
				public void other(
						List<? extends IAbstractInputItem> counterExample) {
					errorHandler.handleSQLError("SQL syntax checker: Syntax error. Most likely, unfinished query", descriptor.getPosition());
					results.add(false);
				}

				@Override
				public void overabstraction(
						List<? extends IAbstractInputItem> counterExample) {
					errorHandler.handleSQLError("SQL syntax checker: Syntactic analysis failed: nesting is too deep in this sentence", descriptor.getPosition());
					results.add(false);
				}
			});
		} catch (MalformedStringLiteralException e) {
			IPosition errorPosition = e.getLiteralPosition();
			if (errorPosition == null) {
				errorPosition = descriptor.getPosition(); 
			}
			errorHandler.handleSQLError("SQL syntax checker: Malformed literal: " + e.getMessage(), errorPosition);
			results.add(false);
		} catch (StackOverflowError e) {  
			// TODO: This hack is no good (see the method above)
			throw e;
		} catch (Throwable e) {
			LOG.exception(e);
			results.add(false);
			throw new CheckerException("SQL syntax checker: internal error: " + e.toString(), descriptor.getPosition());
		}
		
		return !results.contains(false);
	}

	/**
	 * Checks if the string is small enough for the corresponding automaton to fit into memory 
	 */
	public static boolean hasAcceptableSize(IAbstractString abstractString) {
		return AbstractStringSizeCounter.size(abstractString) <= SIZE_THRESHOLD;
	}

	@Override
	public boolean checkAbstractString(IStringNodeDescriptor descriptor,
			ISQLErrorHandler errorHandler, Map<String, String> options)
			throws CheckerException {
		
		IAbstractString abstractString = descriptor.getAbstractValue();
		if (!hasAcceptableSize(abstractString)) {
			if (abstractString instanceof StringChoice) { // This may make things slower, but more precise 
				StringChoice choice = (StringChoice) abstractString;
				boolean hasBigSubstrings = false;
				boolean hasSmallSubstrings = false;
				boolean hasErrors = false;
				for (IAbstractString option : choice.getItems()) {
					if (!hasAcceptableSize(option)) {
						hasBigSubstrings = true;
						hasErrors = true;
					} else {
						try {
							if (!checkStringOfAppropriateSize(errorHandler, descriptor, option)) {
								hasErrors = true;
							}
							hasSmallSubstrings = true;
						} catch (StackOverflowError e) { 
							// TODO: This hack is no good. May be it can be fixed in the FixpointParser   
							hasBigSubstrings = true;
							hasErrors = true;
						}
					}
				}
				if (hasBigSubstrings) {
					errorHandler.handleSQLWarning("SQL syntax checker: SQL string has too many possible variations" + (hasSmallSubstrings ? ". Only some are checked" : ""), descriptor.getPosition());
					return false;
				}
				else {
					return !hasErrors;
				}
			} else {
				errorHandler.handleSQLWarning("SQL syntax checker: SQL string has too many possible variations", descriptor.getPosition());
				return false;
			}
		} else {
			try {
				return checkStringOfAppropriateSize(errorHandler, descriptor, abstractString);
			} catch (StackOverflowError e) {
				// The analyzer has caused a stack overflow in the dfs-based evaluation procedure.
				// See FixpointParser class
				errorHandler.handleSQLWarning("SQL syntax checker: SQL string has too many possible variations", descriptor.getPosition());
				return false;
			}
		}
	}
}
