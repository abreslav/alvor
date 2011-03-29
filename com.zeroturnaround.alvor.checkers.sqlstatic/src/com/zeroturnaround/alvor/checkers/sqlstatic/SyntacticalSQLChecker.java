package com.zeroturnaround.alvor.checkers.sqlstatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.zeroturnaround.alvor.checkers.CheckerException;
import com.zeroturnaround.alvor.checkers.HotspotCheckingResult;
import com.zeroturnaround.alvor.checkers.HotspotError;
import com.zeroturnaround.alvor.checkers.HotspotInfo;
import com.zeroturnaround.alvor.checkers.HotspotWarning;
import com.zeroturnaround.alvor.checkers.IAbstractStringChecker;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
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
	
	private Collection<HotspotCheckingResult> checkStringOfAppropriateSize(
			final StringNodeDescriptor descriptor,
			IAbstractString abstractString) throws CheckerException {
		
		final List<HotspotCheckingResult> result = new ArrayList<HotspotCheckingResult>();
		
		try {
			State automaton = PositionedCharacterUtil.createPositionedAutomaton(abstractString);
			
			ParserSimulator.getGLRInstance().checkAutomaton(automaton, new IParseErrorHandler() {
				
				@Override
				public void unexpectedItem(IAbstractInputItem item,
						List<? extends IAbstractInputItem> counterExampleList) {
					String counterExample = PositionedCharacterUtil.renderCounterExample(counterExampleList);
					Collection<IPosition> markerPositions = PositionedCharacterUtil.getMarkerPositions(((Token) item).getText());
					for (IPosition pos : markerPositions) {
						result.add(new HotspotError(
								"SQL syntax checker: Unexpected token: " + PositionedCharacterUtil.render(item) 
								+ "\n" + "    Counter example: " + counterExample
								, 
								pos));
					}
				}

				@Override
				public void other(
						List<? extends IAbstractInputItem> counterExample) {
					result.add(new HotspotError("SQL syntax checker: Syntax error. Most likely, unfinished query", 
							descriptor.getPosition()));
				}

				@Override
				public void overabstraction(
						List<? extends IAbstractInputItem> counterExample) {
					result.add(new HotspotError("SQL syntax checker: Syntactic analysis failed: nesting is too deep in this sentence", 
							descriptor.getPosition()));
				}
			});
		} catch (MalformedStringLiteralException e) {
			IPosition errorPosition = e.getLiteralPosition();
			if (errorPosition == null) {
				errorPosition = descriptor.getPosition(); 
			}
			result.add(new HotspotError("SQL syntax checker: Malformed literal: " 
					+ e.getMessage(), errorPosition));
		} catch (StackOverflowError e) {  
			// TODO: This hack is no good (see the method above)
			throw e;
		} catch (Throwable e) {
			LOG.exception(e);
			throw new CheckerException("SQL syntax checker: internal error: " + e.toString(), descriptor.getPosition());
		}
		
		return result;
	}

	/**
	 * Checks if the string is small enough for the corresponding automaton to fit into memory 
	 */
	public static boolean hasAcceptableSize(IAbstractString abstractString) {
		return AbstractStringSizeCounter.size(abstractString) <= SIZE_THRESHOLD;
	}

	@Override
	public Collection<HotspotCheckingResult> checkAbstractString(StringNodeDescriptor descriptor,
			ProjectConfiguration configuration)
			throws CheckerException {
		
		List<HotspotCheckingResult> results = new ArrayList<HotspotCheckingResult>();
		
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
							results.addAll(checkStringOfAppropriateSize(descriptor, option));
							hasSmallSubstrings = true;
						} catch (StackOverflowError e) { 
							// TODO: This hack is no good. May be it can be fixed in the FixpointParser   
							hasBigSubstrings = true;
						}
					}
				}
				if (hasBigSubstrings) {
					results.add(new HotspotWarning("SQL syntax checker: SQL string has too many possible variations" 
							+ (hasSmallSubstrings ? ". Only some are checked" : ""), descriptor.getPosition()));
				}
			} else {
				results.add(new HotspotWarning("SQL syntax checker: SQL string has too many possible variations", 
						descriptor.getPosition()));
			}
		} else {
			try {
				return checkStringOfAppropriateSize(descriptor, abstractString);
			} catch (StackOverflowError e) {
				// The analyzer has caused a stack overflow in the dfs-based evaluation procedure.
				// See FixpointParser class
				results.add(new HotspotWarning("SQL syntax checker: SQL string has too many possible variations", descriptor.getPosition()));
			}
		}
		
		if (results.isEmpty()) {
			results.add(new HotspotInfo("SQL syntax check passed", descriptor.getPosition()));
		}
		

		return results;
	}
}
