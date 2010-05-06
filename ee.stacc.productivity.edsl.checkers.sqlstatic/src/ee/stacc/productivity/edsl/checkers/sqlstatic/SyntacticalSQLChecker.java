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
import ee.stacc.productivity.edsl.sqlparser.SQLSyntaxChecker;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;

public class SyntacticalSQLChecker implements IAbstractStringChecker {

	private static int SIZE_THRESHOLD = 25000;
	private static final ILog LOG = Logs.getLog(SyntacticalSQLChecker.class);
	
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
				State automaton = PositionedCharacterUtil.createPositionedAutomaton(abstractString);
				
				SQLSyntaxChecker.INSTANCE.checkAutomaton(automaton, new IParseErrorHandler() {
					
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
				});
			} catch (MalformedStringLiteralException e) {
				errorHandler.handleSQLError("Malformed literal: " + e.getMessage(), descriptor.getPosition());
			} catch (Throwable e) {
				LOG.exception(e);
				errorHandler.handleSQLError("Static checker internal error: " + e.toString(), abstractString.getPosition());
			}
		}
	}

	public static boolean hasAcceptableSize(IAbstractString abstractString) {
		return AbstractStringSizeCounter.size(abstractString) <= SIZE_THRESHOLD;
	}
}
