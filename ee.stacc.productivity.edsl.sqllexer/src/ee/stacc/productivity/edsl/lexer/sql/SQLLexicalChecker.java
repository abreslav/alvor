package ee.stacc.productivity.edsl.lexer.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.automata.AutomataTransduction;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.StringToAutomatonConverter;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.util.AsbtractStringUtils;

/*
 * This class is obsolete
 * 
 * @author abreslav
 */
public class SQLLexicalChecker {

	public static final SQLLexicalChecker INSTANCE = new SQLLexicalChecker();
	private static final ILog LOG = Logs.getLog(SQLLexicalChecker.class);
	
	private SQLLexicalChecker() {}
	
	public List<String> check(IAbstractString str) {
		if (AsbtractStringUtils.hasLoops(str)) {
			throw new IllegalArgumentException("The current version does not support loops in abstract strings");
		}

		State sqlTransducer = SQLLexer.SQL_TRANSDUCER;
		
		State automaton = StringToAutomatonConverter.INSTANCE.convert(str);

		State transduction = AutomataTransduction.INSTANCE.getTransduction(sqlTransducer, automaton, SQLLexer.SQL_ALPHABET_CONVERTER);
		
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
				IAbstractInputItem inChar = transition.getInChar();
				if (inChar.getCode() != -1) {
					String token = SQLLexer.getTokenName(inChar.getCode());
					if (token == null) {
						LOG.error(inChar);
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
