package com.googlecode.alvor.checkers.sqlstatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.googlecode.alvor.checkers.CheckerException;
import com.googlecode.alvor.checkers.HotspotProblem;
import com.googlecode.alvor.checkers.IAbstractStringChecker;
import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.common.logging.ILog;
import com.googlecode.alvor.common.logging.Logs;
import com.googlecode.alvor.configuration.ProjectConfiguration;
import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.lexer.alphabet.Token;
import com.googlecode.alvor.lexer.automata.LexerData;
import com.googlecode.alvor.lexer.automata.State;
import com.googlecode.alvor.sqlparser.GLRStack;
import com.googlecode.alvor.sqlparser.ILRParser;
import com.googlecode.alvor.sqlparser.IParseErrorHandler;
import com.googlecode.alvor.sqlparser.ParserSimulator;
import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.IPosition;
import com.googlecode.alvor.string.StringChoice;
import com.googlecode.alvor.string.util.AbstractStringSizeCounter;

/**
 * Performs syntax checking for abstract strings containing SQL statements
 * Uses "generic" SQL syntax
 * @author abreslav
 *
 */
abstract public class SyntacticalSQLChecker implements IAbstractStringChecker {

	private static final ILog LOG = Logs.getLog(SyntacticalSQLChecker.class);
	private final ParserSimulator<GLRStack> parserSimulator;  

	/**
	 * Maximum size of abstract strings. Bigger strings are likely to cause OutOfMemoryError, 
	 * and must be rejected.
	 */
	private static int SIZE_THRESHOLD = 25000;
	
	public SyntacticalSQLChecker() {
		parserSimulator = new ParserSimulator<GLRStack>(
				this.provideParser(),
				GLRStack.FACTORY, 
				this.provideLexerData());
	}
	
	private Collection<HotspotProblem> checkStringOfAppropriateSize(
			final StringHotspotDescriptor descriptor,
			IAbstractString abstractString) throws CheckerException {
		
		final List<HotspotProblem> result = new ArrayList<HotspotProblem>();
		
		try {
			State automaton = PositionedCharacterUtil.createPositionedAutomaton(abstractString);
			
			parserSimulator.checkAutomaton(automaton, new IParseErrorHandler() {
				
				@Override
				public void unexpectedItem(IAbstractInputItem item,
						List<? extends IAbstractInputItem> counterExampleList) {
					String counterExample = PositionedCharacterUtil.renderCounterExample(counterExampleList);
					Collection<IPosition> markerPositions = PositionedCharacterUtil.getMarkerPositions(((Token) item).getText());
					for (IPosition pos : markerPositions) {
						result.add(new HotspotProblem(
								"SQL syntax checker: Unexpected token: " + PositionedCharacterUtil.render(item) 
								+ "\n" + "    Counter example: " + counterExample
								, 
								pos, HotspotProblem.ProblemType.ERROR));
					}
				}

				@Override
				public void other(
						List<? extends IAbstractInputItem> counterExample) {
					result.add(new HotspotProblem("SQL syntax checker: Syntax error. Most likely, unfinished query", 
							descriptor.getPosition(), HotspotProblem.ProblemType.ERROR));
				}

				@Override
				public void overabstraction(
						List<? extends IAbstractInputItem> counterExample) {
					result.add(new HotspotProblem("SQL syntax checker: Syntactic analysis failed: nesting is too deep in this sentence", 
							descriptor.getPosition(), HotspotProblem.ProblemType.ERROR));
				}
			});
		} catch (MalformedStringLiteralException e) {
			IPosition errorPosition = e.getLiteralPosition();
			if (errorPosition == null) {
				errorPosition = descriptor.getPosition(); 
			}
			result.add(new HotspotProblem("SQL syntax checker: Malformed literal: " 
					+ e.getMessage(), errorPosition, HotspotProblem.ProblemType.ERROR));
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
	public Collection<HotspotProblem> checkAbstractString(StringHotspotDescriptor descriptor,
			String projectName, ProjectConfiguration configuration)
			throws CheckerException {
		
		List<HotspotProblem> results = new ArrayList<HotspotProblem>();
		
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
					results.add(new HotspotProblem("SQL syntax checker: SQL string has too many possible variations" 
							+ (hasSmallSubstrings ? ". Only some are checked" : ""), 
							descriptor.getPosition(), HotspotProblem.ProblemType.UNSUPPORTED));
				}
			} else {
				results.add(new HotspotProblem("SQL syntax checker: SQL string has too many possible variations", 
						descriptor.getPosition(),
						HotspotProblem.ProblemType.UNSUPPORTED));
			}
		} else {
			try {
				return checkStringOfAppropriateSize(descriptor, abstractString);
			} catch (StackOverflowError e) {
				// The analyzer has caused a stack overflow in the dfs-based evaluation procedure.
				// See FixpointParser class
				results.add(new HotspotProblem("SQL syntax checker: SQL string has too many possible variations", 
						descriptor.getPosition(), HotspotProblem.ProblemType.UNSUPPORTED));
			}
		}
		
		return results;
	}
	
	protected abstract ILRParser<GLRStack> provideParser();
	protected abstract LexerData provideLexerData();
	
}
