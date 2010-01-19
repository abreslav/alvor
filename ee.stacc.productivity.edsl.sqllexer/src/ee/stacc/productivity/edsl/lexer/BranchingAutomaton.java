package ee.stacc.productivity.edsl.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ee.stacc.productivity.edsl.strings.CharacterSetFactory;
import ee.stacc.productivity.edsl.strings.CharacterSetString;
import ee.stacc.productivity.edsl.strings.IAbstractString;
import ee.stacc.productivity.edsl.strings.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.strings.ICharacterSet;
import ee.stacc.productivity.edsl.strings.StringChoice;
import ee.stacc.productivity.edsl.strings.StringIteration;
import ee.stacc.productivity.edsl.strings.StringLiteral;
import ee.stacc.productivity.edsl.strings.StringSequence;
import ee.stacc.productivity.edsl.strings.StringVariable;

/**
 * 
 * @author abreslav
 *
 * Rough edge:
 * 
 * This dirty code performs a check, whether the given abstract string 
 * (with simple loops) is accepted by the given automaton.
 *
 * The assumptions are:
 *   *) Every join (after choice) must be synchronized in state
 *   *) WEAK: Every loop exits with the same state as it starts 
 *     Alternative: 
 *     *) We use "state sets" of the underlying automaton, so that
 *        if the loop (after different numbers of iterations)
 *        ends in different states, we continue with the set of states
 *        applying the transition function to all of them simultaneously.
 *
 * Advantages: linear in time over the input. 
 * May be used to: perform lexical analysis before abstract parsing.
 * This will allow for more natural definitions of grammars, avoiding 
 * problems with whitespace management and (?) for better lookahead
 * (see some paper on abstract parsing (the follow-up for the LR-paper).
 * 
 * To handle loops properly: see the todo comment below
 * 
 * To be useful, this should produce an "abstract string over the alphabet of tokens".
 * So the automaton must be replaced with a transducer.
 */
public class BranchingAutomaton {

	private static int totalStates = 0;
	private static int totalAutomata = 0;
	private static int totalTransitions = 0;
	private static int totalVisitors = 0;
	
	private final Collection<State> states;
	private State initialState;
	private State currentState;

	public BranchingAutomaton(Collection<State> states, State initialState,
			State currentState) {
		totalAutomata++;
		this.states = states;
		this.initialState = initialState;
		this.currentState = currentState;
	}

	public BranchingAutomaton(String text) {
		totalAutomata++;
		states = new ArrayList<State>();
		text = text.replace('-', ' ');
		String[] lines = text.split(";");
		
		Map<String, State> map = new HashMap<String, State>();
		for (String line : lines) {
			Pattern pattern = Pattern.compile("([A-Za-z])+(:([A-Za-z]))?");
			Matcher matcher = pattern.matcher(line);
			if (!matcher.find()) {
				continue;
			}
			
			State from = getState(map, matcher.group(1));
			if (initialState == null) {
				initialState = from;
			}
			while (matcher.find()) {
				State to = getState(map, matcher.group(1));
				char c = matcher.group(3).charAt(0);
				ICharacterSet range = CharacterSetFactory.range(c, c);
				Transition transition = new Transition(from, to, range);
				from.outgoingTransitions.add(transition);
				to.incomingTransitions.add(transition);
			}
		}
		currentState = initialState;
	}	

	private State getState(Map<String, State> map, String name) {
		State state = map.get(name);
		if (state == null) {
			state = new State(name);
			states.add(state);
			map.put(name, state);
		}
		return state;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (State state : states) {
			stringBuilder
				.append(state);
			if (!state.outgoingTransitions.isEmpty()) {
				stringBuilder.append(" -> ");
			}
			for (Transition transition : state.outgoingTransitions) {
				stringBuilder
					.append(transition.to)
					.append(transition.set)
					.append(" ");
			}
			stringBuilder.append(";\n");
		}
		return stringBuilder.toString();
	}
	
	public void process(char c) {
		for (Transition transition : currentState.outgoingTransitions) {
			if (transition.set.contains(c)) {
				currentState = transition.to;
				System.out.println(System.identityHashCode(this) + ": " + transition);
				return;
			}
		}
		throw new IllegalStateException("'" + c + "' is not accepted at " + currentState);
	}
	
	public void process(ICharacterSet set) {
		for (Transition transition : currentState.outgoingTransitions) {
			if (transition.set.contains(set)) {
				System.out.println(System.identityHashCode(this) + ": " + transition);
				currentState = transition.to;
				return;
			}
		}
		throw new IllegalStateException();
	}
	
	public void reset() {
		currentState = initialState;
	}
	
	public BranchingAutomaton copy() {
		return new BranchingAutomaton(states, initialState, currentState);
	}
	
	@SuppressWarnings("serial")
	private static final class AnswerException extends RuntimeException {

		public AnswerException(String message) {
			super(message);
		}
		
	}
	
	public static State check(final BranchingAutomaton automaton, IAbstractString str) {
		final State[] answer = new State[1];
		totalVisitors ++;
		IAbstractStringVisitor visitor = new IAbstractStringVisitor() {
			
			@Override
			public void visitStringVariable(StringVariable stringVariable) {
				throw new IllegalArgumentException();
			}
			
			@Override
			public void visitStringSequence(StringSequence stringSequence) {
				State state = null;
				for (int i = 0; i < stringSequence.size(); i++) {
					state = check(automaton, stringSequence.get(i));
				}
				if (state == null) {
					throw new IllegalStateException();
				}
				answer[0] = state;
			}
			
			@Override
			public void visitStringLiteral(StringLiteral stringLiteral) {
				String literal = stringLiteral.getLiteral();
				for (int i = 0; i < literal.length(); i++) {
					char c = literal.charAt(i);
					automaton.process(c);
				}
				answer[0] = automaton.currentState;
			}
			
			@Override
			public void visitStringChoise(StringChoice stringChoise) {
				State agreePoint = null;
				for (int i = 0; i < stringChoise.size(); i++) {
					State state = check(automaton.copy(), stringChoise.get(i));
					if (agreePoint == null) {
						agreePoint = state;
					} else {
						if (agreePoint != state) {
							throw new AnswerException(agreePoint + " != " + state);
						}
					}
				}
				automaton.currentState = agreePoint;
				answer[0] = agreePoint;
			}
			
			@Override
			public void visitCharacterSet(CharacterSetString characterSet) {
				automaton.process(characterSet.getSet());
				answer[0] = automaton.currentState;
			}

			@Override
			public void visitStringIteration(StringIteration stringIteration) {
				IAbstractString body = stringIteration.getBody();
				State state = automaton.currentState;
				State check = check(automaton, body);
				if (state != check) {
					throw new AnswerException("Loop error: " + check + " != " + state);
				}
				answer[0] = state;
				// TODO: a fix point
				// It makes the process heavier since
				// we have to continue the check from 
				// all the reached states
//				Set<State> states = new HashSet<State>();
//				while (true) {
//					State check = check(automaton, body);
//					if (states.contains(check)) {
//						break;
//					}
//					states.add(check);
//				}
				
			}
		};
		str.visit(visitor);
		return answer[0];
	}
	
	public static IAbstractString buildBinary(String s) {
		if (s.length() == 1) {
			return new StringLiteral(s);
		}
		IAbstractString buildBinary = buildBinary(s.substring(1));
		return new StringSequence(
				new StringLiteral(s.charAt(0) + ""), 
				new StringChoice(buildBinary, buildBinary)) ;
	}

	public static void main(String[] args) {
		BranchingAutomaton automaton = new BranchingAutomaton(
				"I - I:x A:q B:a; " +
				"A - B:a; " +
				"B - C:b; " +
				"C - A:c Y:y; " +
				"Y - Y:y; " +
				"");
		System.out.println(automaton);
		IAbstractString s = new StringSequence(
				new StringIteration(
						new StringLiteral("xxxx")
				),
				new StringLiteral("xxq"),
				new StringChoice(
						new StringSequence(
							new StringIteration(
								new StringChoice(
										new StringLiteral(""),
										new StringLiteral("abcabc"),
										new StringLiteral("abc")						
								)
							),
							new StringLiteral("ab")
						),
						new StringLiteral("ab"),
						new StringLiteral("ab"),
						new StringLiteral("ab")						
				),
				new StringLiteral("cabyyyy")
		);
		IAbstractString bin = buildBinary("xxxxqabcabyyyyy");
//		System.out.println(bin);
		System.out.println(check(automaton, s));
		System.out.println("Autoamata: " + totalAutomata);
		System.out.println("Visitors: " + totalVisitors);
		System.out.println("States: " + totalStates);
		System.out.println("Transitions: " + totalTransitions);
	}
	
}
