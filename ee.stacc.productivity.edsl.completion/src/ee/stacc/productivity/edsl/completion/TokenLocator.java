package ee.stacc.productivity.edsl.completion;

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.checkers.sqlstatic.SyntacticalSQLChecker;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.Position;

public class TokenLocator {

	public static final TokenLocator INSTANCE = new TokenLocator();
	
	public IPosition locateToken(String filePath, int offset) {
		IAbstractString abstractString = CacheService.getCacheService().getContainingAbstractString(filePath, offset);
		if (abstractString == null) {
			return null;
		}
		State automaton = SyntacticalSQLChecker.createPositionedAutomaton(abstractString);
		return abstractString.getPosition();
	}
}
