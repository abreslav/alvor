package ee.stacc.productivity.edsl.lexer.automata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;

public class StringToAutomatonConverter {

	public static final StringToAutomatonConverter INSTANCE = new StringToAutomatonConverter();
	
	private StringToAutomatonConverter() {}
	
	public State convert(IAbstractString string) {
		return convert(string, IAlphabetConverter.ID);
	}
	
	public State convert(IAbstractString string, IAlphabetConverter inputConverter) {
		State initial = new State("START", false);
		State eof = new State("EOF", true);
		Set<State> finalStates = new StringToAutomatonConverterVisitor(inputConverter)
					.convert(string, initial);
		for (State state : finalStates) {
			createTransition(state, eof, -1, inputConverter);
		}
		return initial;
	}
	
	private static final class StringToAutomatonConverterVisitor implements
			IAbstractStringVisitor<Set<State>, State> {
		
		private final IAlphabetConverter inputConverter;
		
		public StringToAutomatonConverterVisitor(
				IAlphabetConverter inputConverter) {
			this.inputConverter = inputConverter;
		}

		public Set<State> convert(IAbstractString str, State initial) {
			return str.accept(this, initial);
		}

		@Override
		public Set<State> visitStringCharacterSet(
				StringCharacterSet characterSet, State initial) {
			State fin = new State("F", false);
			for (Character character : characterSet.getContents()) {
				createTransition(initial, fin, (int) character);
			}
			return Collections.singleton(fin);
		}

		@Override
		public Set<State> visitStringConstant(StringConstant stringConstant,
				State initial) {
			State fin = new State("F", false);
			String string = stringConstant.getConstant();
			State current = initial;
			int length = string.length();
			for (int i = 0; i < length; i++) {
				State dest;
				if (i == length - 1) {
					dest = fin;
				} else {
					dest = new State("I", false);
				}
				createTransition(current, dest, (int) string.charAt(i));
				current = dest;
			}
			return Collections.singleton(fin);
		}

		@Override
		public Set<State> visitStringChoise(StringChoice stringChoise,
				State initial) {
			Set<State> result = new HashSet<State>();
			for (IAbstractString item : stringChoise.getItems()) {
				Set<State> finalStates = convert(item, initial);
				result.addAll(finalStates);
			}
			return result;
		}

		@Override
		public Set<State> visitStringRepetition(
				StringRepetition stringRepetition, State initial) {
			IAbstractString body = stringRepetition.getBody();
			State fake = new State("fake", false);
			Set<State> finalStates = convert(body, fake);
			Collection<Transition> initialTransitions = fake.getOutgoingTransitions();
			for (Transition initialTransition : initialTransitions) {
				copyTransition(initial, initialTransition);
			}
			for (State state : finalStates) {
				for (Transition initialTransition : initialTransitions) {
					copyTransition(state, initialTransition);
				}
			}
			return finalStates;
		}

		private void copyTransition(State state, Transition transition) {
			StringToAutomatonConverter.createTransition(state, transition.getTo(), transition.getInChar(), IAlphabetConverter.ID);
		}

		@Override
		public Set<State> visitStringSequence(StringSequence stringSequence,
				State initial) {
			Set<State> current = Collections.singleton(initial);
			for (IAbstractString item : stringSequence.getItems()) {
				Set<State> newCurrent = new HashSet<State>();
				for (State state : current) {
					Set<State> finalStates = convert(item, state);
					newCurrent.addAll(finalStates);
				}
				current = newCurrent;
			}
			return current;
		}
		
		private Transition createTransition(State initial, State fin, int character) {
			return StringToAutomatonConverter.createTransition(initial, fin, character, inputConverter);
		}
	}
	
	private static Transition createTransition(State initial, State fin,
			int character, IAlphabetConverter inputConverter) {
		int convert = inputConverter.convert(character);
		Transition transition = new Transition(initial, fin, convert, "" + (char) convert);
		initial.getOutgoingTransitions().add(transition);
		return transition;
	}
}
