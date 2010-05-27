package ee.stacc.productivity.edsl.sqlparser;

import java.util.Map;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public interface ILRParser<S> {
	S processToken(IAbstractInputItem token, int tokenIndex, S stack);

	IParserState getInitialState();

	int getEofTokenIndex();
	public Map<String, Integer> getNamesToTokenNumbers();
}
