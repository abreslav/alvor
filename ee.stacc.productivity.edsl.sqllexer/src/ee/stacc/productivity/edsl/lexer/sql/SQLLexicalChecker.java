package ee.stacc.productivity.edsl.lexer.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ee.stacc.productivity.edsl.lexer.automata.AutomataInclusion;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.StringToAutomatonConverter;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.sqllexer.SQLLexerData;
import ee.stacc.productivity.edsl.string.IAbstractString;

public class SQLLexicalChecker {

	public static final SQLLexicalChecker INSTANCE = new SQLLexicalChecker();
	
	private SQLLexicalChecker() {}
	
	public List<String> check(IAbstractString str) {
		State sqlTransducer = SQLLexer.SQL_TRANSDUCER;
		
		State automaton = StringToAutomatonConverter.INSTANCE.convert(str, SQLLexer.SQL_ALPHABET_CONVERTER);

		State transduction = AutomataInclusion.INSTANCE.getTrasduction(sqlTransducer, automaton);
		
		Collection<String> errorTokens = new ArrayList<String>();
		findErrorTokens(transduction, new HashSet<Transition>(), errorTokens);
		
		List<String> result = new ArrayList<String>();
		for (String token : errorTokens) {
			result.add("Erroneous token: " + token);
		}
		
		return result;
	}

	private void findErrorTokens(State state, HashSet<Transition> visited,
			Collection<String> errorTokens) {
		for (Transition transition : state.getOutgoingTransitions()) {
			if (!visited.add(transition)) {
				continue;
			}
			if (!transition.isEmpty()) {
				int inChar = transition.getInChar();
				if (inChar != (char) -1) {
					String token = SQLLexerData.TOKENS[inChar];
					if (token == null) {
						System.err.println(inChar);
					}
					if (token.endsWith("_ERR")) {
						errorTokens.add(token);
					}
				}
			}
			findErrorTokens(transition.getTo(), visited, errorTokens);
		}
	}
}
