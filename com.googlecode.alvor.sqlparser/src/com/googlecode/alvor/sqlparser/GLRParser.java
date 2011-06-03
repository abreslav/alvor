package com.googlecode.alvor.sqlparser;

import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;

/**
 * Represents a GLR-parser as a static structure (or states and actions) and a set of processing rules.
 * 
 * @author abreslav
 *
 */
public class GLRParser implements ILRParser<GLRStack> {

	/**
	 * The accepting action of this parser  
	 */
	private static final IAction ACCEPT_ACTION = new AcceptAction();
	
	/**
	 * A grammar rule (we'd better say "production", there can be several 
	 * "rules" with the same LHS) 
	 */
	private static final class Rule {
		private final int lhsSymbol; 
		private final int rhsLength; 
		private final String text;
		
		public Rule(int lhsSymbol, int rhsLength, String text) {
			this.lhsSymbol = lhsSymbol;
			this.rhsLength = rhsLength;
			this.text = text;
		}

		/**
		 * @return number of symbols on the right-hand side 
		 */
		public int getRhsLength() {
			return rhsLength;
		}

		/**
		 * @return the symbol on the left-hand side
		 */
		public int getLhsSymbol() {
			return lhsSymbol;
		}
		
		@Override
		public String toString() {
			return text;
		}
	}
	
	/**
	 * Represents states of this parser 
	 */
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

	/**
	 * An actions of the "GOTO table"
	 */
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
				throw new IllegalStateException("GOTO table ambiguities are not supported");
			}
			IAction action = actions.iterator().next();
			return action.process(null, newStack);
		}
		
		@Override
		public String toString() {
			return "REDUCE " + byRule;
		}
	}

	/**
	 * Load parser tables from an XML file generated by Bison 
	 * @param xmlFile the URL of the file
	 * @return a parser object
	 */
	public static GLRParser build(URL xmlFile) {
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
		
		return new GLRParser(states.get(0), symbolByToken, namesToTokenNumbers);
	}

	private static <T> void addTo(final List<T> list, int number, T element) {
		while (list.size() <= number) {
			list.add(null);
		}
		list.set(number, element);
	}

	/**
	 * Maps token codes (lexer's output alphabet) onto symbol numbers (parser's input alphabet)
	 */
	private final Integer[] symbolByToken;
	/**
	 * The initial state of the parser
	 */
	private final State initialState;
	/**
	 * Maps symbol names to lexer's output alphabet symbols
	 */
	private final Map<String, Integer> namesToTokenNumbers;
	/**
	 * Maps parser's input alphabet codes to names
	 */
	private final Map<Integer, String> symbolNumbersToNames;
	// For debugging purposes
	private PrintStream trace;
	
	private GLRParser(State initialState, List<Integer> symbolByToken, Map<String, Integer> namesToTokenNumbers) {
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

	/**
	 * For debugging purposes
	 */
	public void setTrace(PrintStream trace) {
		this.trace = trace;
	}
	
	@Override
	public int getEofTokenIndex() {
		return getNamesToTokenNumbers().get("$end");
	}
	
	// Proceeds until ERROR, ACCEPT or consumption of a given token 
	@Override
	public GLRStack processToken(IAbstractInputItem token, int tokenIndex, GLRStack glrStack) {
		if (glrStack.hasErrorOnTop()) {
			return glrStack;
		}

		int symbolNumber = symbolByToken[tokenIndex];
		
		GLRStack result = new GLRStack();
		Queue<IParserStack> queue = new LinkedList<IParserStack>(glrStack.getVariants());
		Set<IParserStack> visited = new HashSet<IParserStack>(glrStack.getVariants());
		
		
		while (!queue.isEmpty()) {
			IParserStack currentStack = queue.poll();
			IParserState currentState = currentStack.top();
			if (currentState.isTerminating()) {
				result.addVariant(currentStack); // terminates, put it to the result
				continue;
			}
			Collection<IAction> actions = currentState.getActions(symbolNumber);
			for (IAction action : actions) {
				IParserStack newStack = action.process(token, currentStack);
				if (action.consumes()) {
					result.addVariant(newStack); // current stack consumed, put it to the result
				} else {
					// Did not consume => needs more processing
					if (!visited.contains(newStack)) {
						queue.offer(newStack);
						visited.add(newStack);
					}
				}
			}
		}
		
		return result;
	}

	@SuppressWarnings("unused")
	private void println(Object o) {
		if (trace != null) {
			trace.println(o.toString());
		}
	}

	@Override
	public IParserState getInitialState() {
		return initialState;
	}
	
	@Override
	public Map<String, Integer> getNamesToTokenNumbers() {
		return namesToTokenNumbers;
	}
	
	@Override
	public Map<Integer, String> getSymbolNumbersToNames() {
		return symbolNumbersToNames;
	}
}
