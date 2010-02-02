package ee.stacc.productivity.edsl.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;
import ee.stacc.productivity.edsl.sqlparser.IStackFactory;

public class SimpleFoldedStack implements IAbstractStack {

	public static final IStackFactory FACTORY = new  IStackFactory() {
		
		@Override
		public IAbstractStack newStack(IParserState state) {
			return new SimpleFoldedStack(state);
		}
	};

	static int counter = 0;
	
	private final Map<IParserState, Set<IParserState>> graph; 
	private final IParserState top;
	private final IParserState bottom;

	public SimpleFoldedStack(IParserState state) {
		this(state, state, Collections.<IParserState, Set<IParserState>>emptyMap());
	}
	
	private SimpleFoldedStack(IParserState top, IParserState bottom, Map<IParserState, Set<IParserState>> graph) {
		this.top = top;
		this.bottom = bottom;
		this.graph = Collections.unmodifiableMap(graph);
//		System.out.println("sf: " + ++counter);
//		System.out.println(this);
	}
	
	@Override
	public IAbstractStack push(IParserState state) {
//		System.out.println("Push " + state + " into " + System.identityHashCode(this));
		Set<IParserState> oldSet = getSet(state);
//		if (state == top && oldSet.contains(top)) {
//			return this;
//		}
		Set<IParserState> newSet;
		if (oldSet.isEmpty()) {
			newSet = Collections.singleton(top);
		} else {
			newSet = new HashSet<IParserState>(oldSet);
			newSet.add(top);
		}
		Map<IParserState, Set<IParserState>> newMap = new HashMap<IParserState, Set<IParserState>>(graph);
		newMap.put(state, newSet);
		return new SimpleFoldedStack(state, bottom, newMap);
	}
	
	@Override
	public Set<IAbstractStack> pop(int count) {
		// This cannot return an empty stack!
		Set<IParserState> newTops = new HashSet<IParserState>();
		getAtDistance(count, top, newTops);
		Set<IAbstractStack> result = new HashSet<IAbstractStack>();
		for (IParserState newTop : newTops) {
			Set<IParserState> visited = new HashSet<IParserState>();
			gatherAllStates(newTop, visited );
			Map<IParserState, Set<IParserState>> newGraph = new HashMap<IParserState, Set<IParserState>>(graph);
			newGraph.keySet().retainAll(visited);
			result.add(
					new SimpleFoldedStack(newTop, bottom, newGraph)
			);
		}
		return result;
	}
	
	private void getAtDistance(int count, IParserState state, Set<IParserState> result) {
		if (count == 0) {
			result.add(state);
			return;
		}
		for (IParserState newState : getSet(state)) {
			getAtDistance(count - 1, newState, result);
		}
	}

	@Override
	public IParserState top() {
		return top;
	}

	private Set<IParserState> getSet(IParserState state) {
		Set<IParserState> set = graph.get(state);
		if (set == null) {
			set = Collections.emptySet();
//			graph.put(state, set);
		}
		return set;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bottom == null) ? 0 : bottom.hashCode());
		result = prime * result + ((graph == null) ? 0 : graph.hashCode());
		result = prime * result + ((top == null) ? 0 : top.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleFoldedStack other = (SimpleFoldedStack) obj;
		if (bottom == null) {
			if (other.bottom != null)
				return false;
		} else if (!bottom.equals(other.bottom))
			return false;
		if (graph == null) {
			if (other.graph != null)
				return false;
		} else if (!graph.equals(other.graph))
			return false;
		if (top == null) {
			if (other.top != null)
				return false;
		} else if (!top.equals(other.top))
			return false;
		return true;
	}

	private void gatherAllStates(IParserState current, Set<IParserState> visited) {
		if (!visited.add(current)) {
			return;
		}
		
		Set<IParserState> set = getSet(current);
		for (IParserState next : set) {
			gatherAllStates(next, visited);
		}
	}
	
	@Override
	public String toString() {
		HashSet<IParserState> states = new HashSet<IParserState>();
		gatherAllStates(top, states);
		StringBuilder builder = new StringBuilder("stack" + System.identityHashCode(this)).append("\n");
		for (IParserState state : states) {
			Set<IParserState> transitions = getSet(state);
			if (state == top) {
				builder.append("t:");
			}
			if (state == bottom) {
				builder.append("b:");
			}
			builder.append(state).append(" --> ");
			for (IParserState toState : transitions) {
				builder.append(toState).append(" ");
			}
			builder.append("\n");
		}
		return builder.toString();
	}
}
