package ee.stacc.productivity.edsl.sqlparser;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jdom.JDOMException;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;

public class LRParser {

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
	
	static final class State implements IParserState {

		private final int index;
		private IAction defaultAction = new ErrorAction(this);
		private List<IAction> actions = null;
		
		public State(int index) {
			this.index = index;
		}

		@Override
		public IAction getAction(int symbolNumber) {
			if (actions == null) {
				return defaultAction;
			}
			if (symbolNumber >= actions.size()) {
				return defaultAction;
			}
			IAction action = actions.get(symbolNumber);
			if (action == null) {
				return defaultAction;
			}
			return action;
		}

		public void addAction(Integer symbolNumber, IAction action) {
			if (symbolNumber < 0) {
				defaultAction = action;
			} else {
				if (actions == null) {
					actions = new ArrayList<IAction>();
				}
				addTo(actions, symbolNumber, action);
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
		public Set<IAbstractStack> process(IAbstractInputItem inputItem, IAbstractStack stack) {
			return Collections.singleton(stack.push(new ErrorState(state, inputItem)));
		}
		
		@Override
		public String toString() {
			return "ERROR after state " + state;
		}
	}
	
	private static final class AcceptAction extends AbstractAction {
		@Override
		public Set<IAbstractStack> process(IAbstractInputItem inputItem, IAbstractStack stack) {
			return Collections.singleton(stack.push(IParserState.ACCEPT));
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
		public Set<IAbstractStack> process(IAbstractInputItem inputItem, IAbstractStack stack) {
			return Collections.singleton(stack.push(toState));
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
		public Set<IAbstractStack> process(IAbstractInputItem inputItem, IAbstractStack stack) {
			return Collections.singleton(stack.push(toState));
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
		public Set<IAbstractStack> process(IAbstractInputItem inputItem, IAbstractStack stack) {
			Set<IAbstractStack> popped = stack.pop(byRule.getRhsLength());
			Set<IAbstractStack> results = new HashSet<IAbstractStack>();
			for (IAbstractStack s : popped) {
				IAction action = s.top().getAction(byRule.getLhsSymbol());
				results.addAll(action.process(null, s));
			}
			return results;
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
		
		return new LRParser(states.get(0), symbolByToken, namesToTokenNumbers);
	}

	private static <T> void addTo(final List<T> list, int number, T element) {
		while (list.size() < number) {
			list.add(null);
		}
		list.add(number, element);
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
	
	// Proceeds until ERROR, ACCEPT or consumption of a given token 
	public Set<IAbstractStack> processToken(IAbstractInputItem token, int tokenIndex, IAbstractStack stack) {

		int symbolNumber = symbolByToken[tokenIndex];
		
		Queue<IAbstractStack> queue = new LinkedList<IAbstractStack>();
		Set<IAbstractStack> visited = new HashSet<IAbstractStack>();
		queue.offer(stack);
		visited.add(stack);
		
		Set<IAbstractStack> result = new HashSet<IAbstractStack>();
		while (!queue.isEmpty()) {
			IAbstractStack currenStack = queue.poll();
//			println(currenStack);
			IParserState currentState = currenStack.top();
			if (currentState.isTerminating()) {
				result.add(currenStack);
				continue;
			}
			IAction action = currentState.getAction(symbolNumber);
//			println(action);
			Set<IAbstractStack> newStacks = action.process(token, currenStack);
//			println("new stacks: " + newStacks);
			if (action.consumes()) {
				result.addAll(newStacks);
			} else {
				for (IAbstractStack newStack : newStacks) {
					if (visited.add(newStack)) {
						queue.offer(newStack);
					}
				}
			}
		}
		return result;
	}

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
