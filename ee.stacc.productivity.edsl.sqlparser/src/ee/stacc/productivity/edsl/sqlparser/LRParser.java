package ee.stacc.productivity.edsl.sqlparser;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jdom.JDOMException;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public class LRParser implements ILRParser<IParserStack> {

	private static final IAction ACCEPT_ACTION = new AcceptAction();
	
	private static final class Rule {
		private final int lhsSymbol; 
		private final int rhsLength; 
		private final String text;
		
		public Rule(int lhsSymbol, int rhsLength, String text) {
			this.lhsSymbol = lhsSymbol;
			this.rhsLength = rhsLength;
			this.text = text;
		}

		public int getRhsLength() {
			return rhsLength;
		}

		public int getLhsSymbol() {
			return lhsSymbol;
		}
		
		@Override
		public String toString() {
			return text;
		}
	}
	
	private static final class State implements IParserState {

		private final int index;
		private Collection<IAction> defaultAction = Collections.<IAction>singleton(new ErrorAction(this));
		private List<Collection<IAction>> actionBySymbol = null;
		
		public State(int index) {
			this.index = index;
		}

		@Override
		public Collection<IAction> getActions(int symbolNumber) {
			if (symbolNumber < 0) {
				return defaultAction;
			}
			if (actionBySymbol == null) {
				return defaultAction;
			}
			if (symbolNumber >= actionBySymbol.size()) {
				return defaultAction;
			}
			Collection<IAction> actions = actionBySymbol.get(symbolNumber);
			if (actions == null) {
				return defaultAction;
			}
			return actions;
		}

		public void addAction(Integer symbolNumber, IAction action) {
			if (symbolNumber < 0) {
				defaultAction = Collections.<IAction>singleton(action);
			} else {
				if (actionBySymbol == null) {
					actionBySymbol = new ArrayList<Collection<IAction>>();
				}
				if (actionBySymbol.size() <= symbolNumber) {
					addTo(actionBySymbol, symbolNumber, null);
				}
				Collection<IAction> collection = actionBySymbol.get(symbolNumber);
				if (collection == null) {
					collection = new ArrayList<IAction>();
					actionBySymbol.set(symbolNumber, collection);
				}
				collection.add(action);
			}
		}

		@Override
		public boolean isTerminating() {
			return false;
		}
		
		@Override
		public boolean isError() {
			return false;
		}
		
		@Override
		public String toString() {
			return "S" + index;
		}
	}

	private static final class ErrorAction extends AbstractAction {
		
		private final State state;
		
		public ErrorAction(State state) {
			this.state = state;
		}

		@Override
		public IParserStack process(IAbstractInputItem inputItem, IParserStack stack) {
			return stack.push(new ErrorState(state, inputItem));
		}
		
		@Override
		public String toString() {
			return "ERROR after state " + state;
		}
	}
	
	private static final class AcceptAction extends AbstractAction {
		@Override
		public IParserStack process(IAbstractInputItem inputItem, IParserStack stack) {
			return stack.push(IParserState.ACCEPT);
		}
		
		@Override
		public String toString() {
			return "ACCEPT";
		}
	}
	
	private static final class GotoAction extends AbstractAction {
		private final State toState;
		
		public GotoAction(State toState) {
			this.toState = toState;
		}

		@Override
		public IParserStack process(IAbstractInputItem inputItem, IParserStack stack) {
			return stack.push(toState);
		}
		
		@Override
		public String toString() {
			return "GOTO " + toState;
		}
	}
	
	private static final class ShiftAction implements IAction {
		private final IParserState toState;
		
		public ShiftAction(IParserState toState) {
			this.toState = toState;
		}

		@Override
		public IParserStack process(IAbstractInputItem inputItem, IParserStack stack) {
			return stack.push(toState);
		}

		@Override
		public boolean consumes() {
			return true;
		}
		
		@Override
		public boolean isError() {
			return false;
		}
		
		@Override
		public String toString() {
			return "SHIFT " + toState;
		}
	}
	
	private static final class ReduceAction extends AbstractAction {
		private final Rule byRule;
		
		public ReduceAction(Rule byRule) {
			this.byRule = byRule;
		}

		@Override
		public IParserStack process(IAbstractInputItem inputItem, IParserStack stack) {
			IParserStack newStack = stack.pop(byRule.getRhsLength());
			Collection<IAction> actions = newStack.top().getActions(byRule.getLhsSymbol());
			if (actions.size() != 1) {
				throw new IllegalStateException("No GLR yet");
			}
			IAction action = actions.iterator().next();
			return action.process(null, newStack);
		}
		
		@Override
		public String toString() {
			return "REDUCE " + byRule;
		}
	}

	public static LRParser build(URL xmlFile) throws JDOMException, IOException {
		final List<State> states = new ArrayList<State>();
		final List<Rule> rules = new ArrayList<Rule>();
		final List<Integer> symbolByToken = new ArrayList<Integer>();
		final Map<String, Integer> namesToSymbolNumbers = new HashMap<String, Integer>();
		final Map<String, Integer> namesToTokenNumbers = new HashMap<String, Integer>();
		namesToSymbolNumbers.put("$default", -1);
		LRParserLoader.load(xmlFile, new ILRParserBuilder() {
			
			@Override
			public void createState(int number) {
				addTo(states, number, new State(number));
			}
			
			@Override
			public void addTerminal(int symbolNumber, int tokenIndex, String name) {
				addTo(symbolByToken, tokenIndex, symbolNumber);
				namesToSymbolNumbers.put(name, symbolNumber);
				namesToTokenNumbers.put(name, tokenIndex);
			}
			
			@Override
			public void addRule(int number, String lhs, int rhsLength, String text) {
				Integer lhsNumber = namesToSymbolNumbers.get(lhs);
				addTo(rules, number, new Rule(lhsNumber, rhsLength, "(" + number + ") " + text));
			}
			
			@Override
			public void addNonterminal(int symbolNumber, String name) {
				namesToSymbolNumbers.put(name, symbolNumber);
			}
			
			@Override
			public void addShiftAction(int stateNumber, String symbol, int toState) {
				State state = states.get(stateNumber);
				Integer symbolNumber = namesToSymbolNumbers.get(symbol);
				state.addAction(symbolNumber, new ShiftAction(states.get(toState)));
			}
			
			@Override
			public void addReduceAction(int stateNumber, String symbol, int rule) {
				State state = states.get(stateNumber);
				Integer symbolNumber = namesToSymbolNumbers.get(symbol);
				state.addAction(symbolNumber, new ReduceAction(rules.get(rule)));
			}
			
			@Override
			public void addAcceptAction(int stateNumber, String symbol) {
				State state = states.get(stateNumber);
				Integer symbolNumber = namesToSymbolNumbers.get(symbol);
				state.addAction(symbolNumber, ACCEPT_ACTION);
			}
			
			@Override
			public void addGotoAction(int stateNumber, String symbol, int toState) {
				State state = states.get(stateNumber);
				Integer symbolNumber = namesToSymbolNumbers.get(symbol);
				state.addAction(symbolNumber, new GotoAction(states.get(toState)));
			}
		});
		
		System.err.println("Rechecking LR parser on load!!!");
		System.err.println("Rechecking LR parser on load!!!");
		System.err.println("Rechecking LR parser on load!!!");
		System.err.println("Rechecking LR parser on load!!!");
		LRParserLoader.load(xmlFile, new ILRParserBuilder() {
			
			@Override
			public void createState(int number) {
				check(states.get(number).index == number);
			}
			
			private void check(boolean b) {
				if (!b) {
					throw new AssertionError();
				}
			}

			private void check(Object message, boolean b) {
				if (!b) {
					throw new AssertionError(message);
				}
			}
			
			@Override
			public void addTerminal(int symbolNumber, int tokenIndex, String name) {
			}
			
			@Override
			public void addRule(int number, String lhs, int rhsLength, String text) {
			}
			
			@Override
			public void addNonterminal(int symbolNumber, String name) {
			}
			
			@Override
			public void addShiftAction(int stateNumber, String symbol, int toState) {
				State state = states.get(stateNumber);
				Integer symbolNumber = namesToSymbolNumbers.get(symbol);
				Collection<IAction> actions = state.getActions(symbolNumber);
				check(actions.size() == 1);
				IAction action = actions.iterator().next();
				check(action, action instanceof ShiftAction);
				check(((State)((ShiftAction) action).toState).index == toState);
			}
			
			@Override
			public void addReduceAction(int stateNumber, String symbol, int rule) {
				State state = states.get(stateNumber);
				Integer symbolNumber = namesToSymbolNumbers.get(symbol);
//				state.addAction(symbolNumber, new ReduceAction(rules.get(rule)));
				Collection<IAction> actions = state.getActions(symbolNumber);
				check(actions.size() == 1);
				IAction action = actions.iterator().next();
				check(action instanceof ReduceAction);
				check(((ReduceAction) action).byRule == rules.get(rule));
			}
			
			@Override
			public void addAcceptAction(int stateNumber, String symbol) {
				State state = states.get(stateNumber);
				Integer symbolNumber = namesToSymbolNumbers.get(symbol);
//				state.addAction(symbolNumber, ACCEPT_ACTION);
				Collection<IAction> actions = state.getActions(symbolNumber);
				check(actions.size() == 1);
				IAction action = actions.iterator().next();
				check(action == ACCEPT_ACTION);
			}
			
			@Override
			public void addGotoAction(int stateNumber, String symbol, int toState) {
				State state = states.get(stateNumber);
				Integer symbolNumber = namesToSymbolNumbers.get(symbol);
//				state.addAction(symbolNumber, new GotoAction(states.get(toState)));
				Collection<IAction> actions = state.getActions(symbolNumber);
				check(actions.size() == 1);
				IAction action = actions.iterator().next();
				check(action instanceof GotoAction);
				check(((State)((GotoAction) action).toState).index == toState);
			}
		});
		
		return new LRParser(states.get(0), symbolByToken, namesToTokenNumbers);
	}

	private static <T> void addTo(final List<T> list, int number, T element) {
		while (list.size() <= number) {
			list.add(null);
		}
		list.set(number, element);
	}

	private final Integer[] symbolByToken;
	private final State initialState;
	private final Map<String, Integer> namesToTokenNumbers;
	private final Map<Integer, String> symbolNumbersToNames;
	private PrintStream trace;
	
	private LRParser(State initialState, List<Integer> symbolByToken, Map<String, Integer> namesToTokenNumbers) {
		this.initialState = initialState;
		this.symbolByToken = new Integer[symbolByToken.size()];
		symbolByToken.toArray(this.symbolByToken);

		this.namesToTokenNumbers = Collections.unmodifiableMap(new TreeMap<String, Integer>(namesToTokenNumbers));
		Map<Integer, String> map = new HashMap<Integer, String>();
		for (Entry<String, Integer> entry : this.namesToTokenNumbers.entrySet()) {
			map.put(this.symbolByToken[entry.getValue()], entry.getKey());
		}
		this.symbolNumbersToNames = Collections.unmodifiableMap(map);
		
	}

	public void setTrace(PrintStream trace) {
		this.trace = trace;
	}
	
	@Override
	public int getEofTokenIndex() {
		return getNamesToTokenNumbers().get("$end");
	}
	
	// Proceeds until ERROR, ACCEPT or consumption of a given token
	@Override
	public IParserStack processToken(IAbstractInputItem token, int tokenIndex, IParserStack stack) {

		int symbolNumber = symbolByToken[tokenIndex];
		
		IParserStack currentStack = stack;
		while (true) {
			IParserState currentState = currentStack.top();
			if (currentState.isTerminating()) {
				return currentStack;
			}
			Collection<IAction> actions = currentState.getActions(symbolNumber);
			if (actions.size() != 1) {
				throw new IllegalStateException("No GLR yet");
			}
			IAction action = actions.iterator().next();
			currentStack = action.process(token, currentStack);
			if (action.consumes()) {
				return currentStack;
			}
		}
	}

	@SuppressWarnings("unused")
	private void println(Object o) {
		if (trace != null) {
			trace.println(o.toString());
		}
	}
	
	public IParserState getInitialState() {
		return initialState;
	}
	
	public Map<String, Integer> getNamesToTokenNumbers() {
		return namesToTokenNumbers;
	}
	
	public Map<Integer, String> getSymbolNumbersToNames() {
		return symbolNumbersToNames;
	}
}
