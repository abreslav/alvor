package com.googlecode.alvor.lexer.automata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.lexer.alphabet.Yield;
import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.IAbstractStringVisitor;
import com.googlecode.alvor.string.StringCharacterSet;
import com.googlecode.alvor.string.StringChoice;
import com.googlecode.alvor.string.StringConstant;
import com.googlecode.alvor.string.StringParameter;
import com.googlecode.alvor.string.StringRecursion;
import com.googlecode.alvor.string.StringRepetition;
import com.googlecode.alvor.string.StringSequence;

/**
 * Converts an abstract string to a (nondeterministic) automaton
 * 
 * @author abreslav
 *
 */
public class StringToAutomatonConverter {

	/**
	 * Singleton instance. Use instead of creating objects of this class
	 */
	public static final StringToAutomatonConverter INSTANCE = new StringToAutomatonConverter();
	
	private StringToAutomatonConverter() {}
	
	/**
	 * Converts an abstract string into an automaton with simple characters on edges
	 */
	public State convert(IAbstractString string) {
		return convert(string, SimpleCharacterFactory.INSTANCE);
	}
	
	/**
	 * Converts an abstract string into a (nondeterministic) automaton and uses a factory to transform individual 
	 * Unicode characters into {@link IAbstractInputItem}s 
	 * @param string input abstract string
	 * @param factory transforms Unicode characters into {@link IAbstractInputItem}s
	 * @return initial state of the resulting automaton
	 */
	public State convert(IAbstractString string, IInputItemFactory factory) {
		State initial = new State("START", false);
		State eof = new State("EOF", true);
		Set<State> finalStates = new StringToAutomatonConverterVisitor(factory)
					.convert(string, initial);
		for (State state : finalStates) {
			createTransition(state, eof, IAbstractInputItem.EOF);
		}
		return initial;
	}
	
	private static final class StringToAutomatonConverterVisitor implements
			IAbstractStringVisitor<Set<State>, State> {
		
		private final IInputItemFactory factory;

		public StringToAutomatonConverterVisitor(IInputItemFactory factory) {
			this.factory = factory;
		}

		public Set<State> convert(IAbstractString str, State initial) {
			return str.accept(this, initial);
		}

		@Override
		public Set<State> visitStringCharacterSet(
				StringCharacterSet characterSet, State initial) {
			if (characterSet.isEmpty()) {
				return Collections.singleton(initial);
			}
			State fin = new State("F", false);
			for (Character character : characterSet.getContents()) {
				createTransition(initial, fin, factory.createInputItem(characterSet, (int) character));
			}
			return Collections.singleton(fin);
		}

		@Override
		public Set<State> visitStringParameter(
				StringParameter stringParameter, State initial) {
			throw new IllegalArgumentException();
		}

		@Override
		public Set<State> visitStringConstant(StringConstant stringConstant,
				State initial) {
			if (stringConstant.isEmpty()) {
				return Collections.singleton(initial);
			}
			State fin = new State("F", false);
			State current = initial;
			IAbstractInputItem[] items = factory.createInputItems(stringConstant);
			for (int i = 0; i < items.length; i++) {
				State dest;
				if (i == items.length - 1) {
					dest = fin;
				} else {
					dest = new State("I", false);
				}
				createTransition(current, dest, items[i]);
				current = dest;
			}
			return Collections.singleton(fin);
		}

		@Override
		public Set<State> visitStringChoice(StringChoice stringChoice,
				State initial) {
			if (stringChoice.isEmpty()) {
				return Collections.singleton(initial);
			}
			Set<State> result = new HashSet<State>();
			for (IAbstractString item : stringChoice.getItems()) {
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
			Iterable<Transition> initialTransitions = fake.getOutgoingTransitions();
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
			StringToAutomatonConverter.createTransition(state, transition.getTo(), transition.getInChar());
		}

		@Override
		public Set<State> visitStringSequence(StringSequence stringSequence,
				State initial) {
			Set<State> current = Collections.singleton(initial);
			for (IAbstractString item : stringSequence.getItems()) {
				if (item.isEmpty()) {
					continue;
				}
				Set<State> newCurrent = new HashSet<State>();
				for (State state : current) {
					Set<State> finalStates = convert(item, state);
					newCurrent.addAll(finalStates);
				}
				current = newCurrent;
			}
			return current;
		}

		@Override
		public Set<State> visitStringRecursion(StringRecursion stringRecursion,
				State data) {
			throw new IllegalArgumentException();
		}
		
	}
	
	private static Transition createTransition(State initial, State fin,
			IAbstractInputItem inChar) {
		return Transition.create(initial, fin, inChar, Collections.singletonList(Yield.create(inChar.getCode())));
	}
}
