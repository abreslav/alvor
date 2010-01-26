package ee.stacc.productivity.edsl.sqlparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jdom.JDOMException;

public class LRParser {

	private static final IAction ERROR_ACTION = new ErrorAction();
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
		private IAction defaultAction = null;
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
				return ERROR_ACTION;
			}
			IAction action = actions.get(symbolNumber);
			if (action == null) {
				return ERROR_ACTION;
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
		public String toString() {
			return "S" + index;
		}
	}

	private static abstract class AbstractAction implements IAction {
		@Override
		public boolean consumes() {
			return false;
		}
	}
	
	private static final class ErrorAction extends AbstractAction {
		@Override
		public Set<IAbstractStack> process(int symbolNumber, IAbstractStack stack) {
			return stack.push(IParserState.ERROR);
		}
		
		@Override
		public String toString() {
			return "ERROR";
		}
	}
	
	private static final class AcceptAction extends AbstractAction {
		@Override
		public Set<IAbstractStack> process(int symbolNumber, IAbstractStack stack) {
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
		public Set<IAbstractStack> process(int symbolNumber, IAbstractStack stack) {
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
		public Set<IAbstractStack> process(int symbolNumber, IAbstractStack stack) {
			return stack.push(toState);
		}

		@Override
		public boolean consumes() {
			return true;
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
		public Set<IAbstractStack> process(int symbolNumber, IAbstractStack stack) {
			Set<IAbstractStack> popped = stack.pop(byRule.getRhsLength());
			Set<IAbstractStack> results = new HashSet<IAbstractStack>();
			for (IAbstractStack s : popped) {
				IAction action = s.top().getAction(byRule.getLhsSymbol());
				results.addAll(action.process(-1, s));
			}
			return results;
		}
		
		@Override
		public String toString() {
			return "REDUCE " + byRule;
		}
	}

	public static LRParser build(String xmlFile) throws JDOMException, IOException {
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
//		
//		int max = 0;
//		String[] names = new String[namesToSymbolNumbers.size() + 1];
//		for (Entry<String, Integer> entry : namesToSymbolNumbers.entrySet()) {
////			System.out.println(entry);
//			Integer value = entry.getValue();
//			if (max < value) {
//				max = value;
//			}
//			names[value + 1] = entry.getKey();
//		}
//		
//		for (State state : states) {
//			System.out.println(state);
//			for (int i = 0; i <= max; i++) {
//				System.out.println(names[i + 1] + " " + state.getAction(i));				
//			}
//		}
		
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
	
	private LRParser(State initialState, List<Integer> symbolByToken, Map<String, Integer> namesToTokenNumbers) {
		this.initialState = initialState;
		this.namesToTokenNumbers = Collections.unmodifiableMap(new HashMap<String, Integer>(namesToTokenNumbers));
		this.symbolByToken = new Integer[symbolByToken.size()];
		symbolByToken.toArray(this.symbolByToken);
	}

	// Proceeds until ERROR, ACCEPT or a consumption of a given token 
	public Set<IAbstractStack> processToken(int tokenIndex, IAbstractStack stack) {
		Integer symbolNumber = symbolByToken[tokenIndex];
		Queue<IAbstractStack> queue = new LinkedList<IAbstractStack>();
		queue.offer(stack);
		Set<IAbstractStack> result = new HashSet<IAbstractStack>();
		while (!queue.isEmpty()) {
			IAbstractStack currenStack = queue.poll();
			IParserState currentState = currenStack.top();
			if (currentState.isTerminating()) {
				result.add(currenStack);
				continue;
			}
			IAction action = currentState.getAction(symbolNumber);
			Set<IAbstractStack> newStacks = action.process(symbolNumber, currenStack);
			if (action.consumes()) {
				result.addAll(newStacks);
			} else {
				for (IAbstractStack newStack : newStacks) {
					queue.offer(newStack);
				}
			}
		}
		return result;
	}
	
	public State getInitialState() {
		return initialState;
	}
	
	public Map<String, Integer> getNamesToTokenNumbers() {
		return namesToTokenNumbers;
	}
}
