package ee.stacc.productivity.edsl.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ee.stacc.productivity.edsl.sqlparser.IAbstractStack;
import ee.stacc.productivity.edsl.sqlparser.IParserState;

public class FoldedStack implements IAbstractStack {

	public static final IStackFactory FACTORY = new  IStackFactory() {
		
		@Override
		public IAbstractStack newStack(IParserState state) {
			return new FoldedStack(state);
		}
	};

	static int counter = 0;
	
	private final Map<IParserState, Set<IParserState>> graph; 
	private final FoldedStack parent;
	private final IParserState top;
	private final IParserState bottom;

	public FoldedStack(IParserState state) {
		this(null, state, state, Collections.<IParserState, Set<IParserState>>emptyMap());
	}
	
	private FoldedStack(FoldedStack parent, IParserState top, IParserState bottom, Map<IParserState, Set<IParserState>> graph) {
		this.top = top;
		this.bottom = bottom;
		this.parent = parent;
		this.graph = Collections.unmodifiableMap(graph);
		System.out.println(++counter);
	}
	
	@Override
	public IAbstractStack push(IParserState state) {
		Set<IParserState> oldSet = getSet(state);
		if (state == top && oldSet.contains(top)) {
			return this;
		}
		Set<IParserState> newSet;
		if (oldSet.isEmpty()) {
			newSet = Collections.singleton(top);
		} else {
			newSet = new HashSet<IParserState>(oldSet);
			newSet.add(top);
		}
		Map<IParserState, Set<IParserState>> newMap = Collections.singletonMap(state, newSet);
		return new FoldedStack(this, state, bottom, newMap);
	}
	
	@Override
	public Set<IAbstractStack> pop(int count) {
		// This cannot return an empty stack!
		Set<IParserState> newTops = new HashSet<IParserState>();
		getAtDistance(count, top, newTops);
		Set<IAbstractStack> result = new HashSet<IAbstractStack>();
		for (IParserState newTop : newTops) {
			result.add(
					new FoldedStack(this, newTop, bottom, graph)
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
			if (parent != null) {
				set = parent.getSet(state);
			} else {
				set = Collections.emptySet();
			}
		}
		return set;
	}

	
	@Override
	public int hashCode() {
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (false == obj instanceof FoldedStack) {
			return false;
		}
		FoldedStack other = (FoldedStack) obj;
		Set<IParserState> myAll = new HashSet<IParserState>();
		gatherAllStates(top, myAll);
		Set<IParserState> hisAll = new HashSet<IParserState>();
		gatherAllStates(other.top, hisAll);
		if (!myAll.equals(hisAll)) {
			return false;
		}
		for (IParserState state : hisAll) {
			Set<IParserState> mySet = getSet(state);
			Set<IParserState> hisSet = other.getSet(state);
			if (!mySet.equals(hisSet)) {
				return false;
			}
		}
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
}
