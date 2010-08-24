package com.zeroturnaround.alvor.lexer.automata;


public class FasterStringToAutomatonConverter {

//	public static final FasterStringToAutomatonConverter INSTANCE = new FasterStringToAutomatonConverter();
//
//	private static class Automaton {
//		private final State init;
//		private final State fin;
//		
//		public Automaton(State init, State fin) {
//			this.init = init;
//			this.fin = fin;
//		}
//	}
//	
//	private FasterStringToAutomatonConverter() {}
//	
//	public State convert(IAbstractString string) {
//		return convert(string, IAlphabetConverter.ID);
//	}
//	
//	public State convert(IAbstractString string, IAlphabetConverter inputConverter) {
//		State initial = new State("START", false);
//		State eof = new State("EOF", true);
//		Automaton automaton = new StringToAutomatonConverterVisitor(inputConverter)
//					.convert(string, null);
//		createEmptyTransition(initial, automaton.init);
//		createTransition(automaton.fin, eof, -1, inputConverter);
//		initial = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(initial);
//		return AutomataDeterminator.determinate(initial);
//	}
//	
//	private static final class StringToAutomatonConverterVisitor implements
//			IAbstractStringVisitor<Automaton, Void> {
//		
//		private final IAlphabetConverter inputConverter;
//		
//		public StringToAutomatonConverterVisitor(
//				IAlphabetConverter inputConverter) {
//			this.inputConverter = inputConverter;
//		}
//
//		public Automaton convert(IAbstractString str, Void data) {
//			return str.accept(this, null);
//		}
//
//		@Override
//		public Automaton visitStringCharacterSet(
//				StringCharacterSet characterSet, Void data) {
//			State init = new State("I", false);
//			State fin = new State("F", false);
//			for (Character character : characterSet.getContents()) {
//				createTransition(init, fin, (int) character);
//			}
//			return new Automaton(init, fin);
//		}
//
//		@Override
//		public Automaton visitStringConstant(StringConstant stringConstant,
//				Void data) {
//			State init = new State("I", false);
//			State fin = new State("F", false);
//			String string = stringConstant.getConstant();
//			State current = init;
//			int length = string.length();
//			for (int i = 0; i < length; i++) {
//				State dest;
//				if (i == length - 1) {
//					dest = fin;
//				} else {
//					dest = new State("I", false);
//				}
//				createTransition(current, dest, (int) string.charAt(i));
//				current = dest;
//			}
//			return new Automaton(init, fin);
//		}
//
//		@Override
//		public Automaton visitStringChoise(StringChoice stringChoise,
//				Void data) {
//			State init = new State("I", false);
//			final State fin = new State("F", false);
//			for (IAbstractString item : stringChoise.getItems()) {
//				Automaton automaton = convert(item, null);
//				createEmptyTransition(init, automaton.init);
//				createEmptyTransition(automaton.fin, fin);
//			}
//			HashSet<State> result = new HashSet<State>();
//			State newInit = EmptyTransitionEliminator.INSTANCE.eliminateEmptySetTransitions(init);
//			State detInit = AutomataDeterminator.determinate(newInit, new IStateSetPredicate() {
//				
//				@Override
//				public boolean accept(Set<State> states) {
//					return states.contains(fin);
//				}
//			}, result);
//			return new Automaton(init, fin);
//		}
//
//		@Override
//		public Automaton visitStringRepetition(
//				StringRepetition stringRepetition, Void data) {
//			State init = new State("I", false);
//			State fin = new State("F", false);
//			IAbstractString body = stringRepetition.getBody();
//			Automaton automaton = convert(body, null);
//			createEmptyTransition(init, automaton.init);
//			createEmptyTransition(automaton.fin, fin);
//			return new Automaton(init, fin);
//		}
//
//		@Override
//		public Automaton visitStringSequence(StringSequence stringSequence,
//				Void data) {
//			State init = new State("I", false);
//			State fin = new State("F", false);
//			State currentInit = init;
//			for (IAbstractString item : stringSequence.getItems()) {
//				Automaton automaton = convert(item, null);
//				createEmptyTransition(currentInit, automaton.init);
//				currentInit = automaton.fin;
//			}
//			createEmptyTransition(currentInit, fin);
//			return new Automaton(init, fin);
//		}
//		
//		private Transition createTransition(State initial, State fin, int character) {
//			return FasterStringToAutomatonConverter.createTransition(initial, fin, character, inputConverter);
//		}
//	}
//	
//	private static Transition createTransition(State initial, State fin,
//			int character, IAlphabetConverter inputConverter) {
//		int in = inputConverter.convert(character);
//		Transition transition = new Transition(initial, fin, in, "" + (char) in);
//		initial.getOutgoingTransitions().add(transition);
//		return transition;
//	}
//
//	private static Transition createEmptyTransition(State initial, State fin) {
//		Transition transition = new Transition(initial, fin, null, "\0");
//		initial.getOutgoingTransitions().add(transition);
//		return transition;
//	}
}
