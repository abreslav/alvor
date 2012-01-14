package ru.tolmachev.parsing.conjunctive.stack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ru.tolmachev.core.IAbstractSymbol;
import ru.tolmachev.core.BGTableState;

import com.googlecode.alvor.sqlparser.IAction;
import com.googlecode.alvor.sqlparser.IParserStack;
import com.googlecode.alvor.sqlparser.IParserStackLike;
import com.googlecode.alvor.sqlparser.IParserState;

/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 04.12.11
 * Time: 19:21
 */
public class GraphStack implements IParserStackLike {

	private Set<GraphStackNode> top;

    private Set<GraphStackNode> nextTop;

    private final List<GraphStackNode> stack;

    private final GraphStackNode startNode;

    private Set<GraphStackNode> garbageCollection;

    public GraphStack(GraphStackNode node) {
        startNode = node;
        stack = new LinkedList<GraphStackNode>(Collections.singleton(startNode));
        top = new HashSet<GraphStackNode>(Collections.singleton(startNode));
        nextTop = new HashSet<GraphStackNode>();
        garbageCollection = new HashSet<GraphStackNode>();
	}

	public Set<GraphStackNode> getTop() {
        return top;
    }

    public void addNode(GraphStackNode node) {
        stack.add(node);
    }

    public void addNodeToNextTopLayer(GraphStackNode node) {
        nextTop.add(node);
    }

    public GraphStackNode findNodeInTop(int stateIndex) {
        return findNodeByStateId(top, stateIndex);
    }

    public GraphStackNode findNodeInNextTop(int stateIndex) {
        return findNodeByStateId(nextTop, stateIndex);
    }

    public GraphStackNode findNodeByStateId(Set<GraphStackNode> set, int stateIndex) {
        for (GraphStackNode node : set) {
            BGTableState state = node.getState();
            if (state.getIndex() == stateIndex) {
                return node;
            }
        }
        return null;
    }

    public void addNodeToGarbageCollector(GraphStackNode node) {
        garbageCollection.add(node);
    }

    public boolean isNewTopEmpty() {
        return nextTop.isEmpty();
    }

    public void updateTop() {
        top = nextTop;
        nextTop = new HashSet<GraphStackNode>();
    }

    public void connectNodes(GraphStackNode prev, GraphStackNode next, IAbstractSymbol grammarElement) {
        prev.addSuccessor(grammarElement, next);
        next.addPredecessor(prev);
    }

    public void disconnectNodes(GraphStackNode prev, GraphStackNode next, Iterator<GraphStackNode> iter) {
        prev.removeSuccessor(next);
        iter.remove();
    }


    public void clearGarbageCollector() {
        Set<GraphStackNode> nodesWithoutSuccessors = getNodesWithoutSuccessors(garbageCollection);

        while (nodesWithoutSuccessors.size() > 0) {
            Iterator<GraphStackNode> iter = nodesWithoutSuccessors.iterator();
            while (iter.hasNext()) {
                GraphStackNode node = iter.next();
                if (cutBranch(node)) {
                    iter.remove();
                    stack.remove(node);
                    top.remove(node);
                }
            }
        }
        garbageCollection = new HashSet<GraphStackNode>();
    }

    private Set<GraphStackNode> getNodesWithoutSuccessors(Set<GraphStackNode> setOfNodesToDelete) {
        Set<GraphStackNode> nodesWithoutSuccessors = new HashSet<GraphStackNode>();
        for (GraphStackNode node : setOfNodesToDelete) {
            if (node.getSuccessorsAmount() == 0) {
                nodesWithoutSuccessors.add(node);
            }
        }
        return nodesWithoutSuccessors;
    }

    private boolean cutBranch(GraphStackNode node) {
        if (node.getSuccessorsAmount() != 0) {
            return false;
        }

        List<GraphStackNode> predecessors = node.getPredecessors();
        Iterator<GraphStackNode> iter = predecessors.iterator();
        while (iter.hasNext()) {
            GraphStackNode predecessor = iter.next();
            predecessor.removeSuccessor(node);
            cutBranch(predecessor);
            iter.remove();
            stack.remove(node);
            top.remove(node);
        }
        return true;
    }

    public boolean isNodesConnected(GraphStackNode n1, GraphStackNode n2) {
        List<GraphStackNode> predecessors = n2.getPredecessors();
        for (GraphStackNode predecessor : predecessors) {
            if (predecessor.equals(n1)) {
                return true;
            }
        }
        return false;
    }

    public Collection<IParserState> top() {
        Collection<IParserState> top = new HashSet<IParserState>();
        for (GraphStackNode node : this.top) {
            top.add(node);
        }

        return top;
    }

    public boolean hasErrorOnTop() {
        return getTop().isEmpty();
    }

    public IParserState getErrorOnTop() {
        return new IParserState() {
			
			@Override
			public boolean isTerminating() {
				return true;
			}
			
			@Override
			public boolean isError() {
				return true;
			}
			
			@Override
			public Collection<IAction> getActions(int symbolNumber) {
				return Collections.emptyList();
			}
		};
    }

    public boolean topAccepts() {
        Set<GraphStackNode> top = getTop();
        for (GraphStackNode topLayerNode : top) {
            if (topLayerNode.getState().isAccepting()) {
                List<GraphStackNode> predecessors = topLayerNode.getPredecessors();

                for (GraphStackNode predecessor : predecessors) {
                    if (predecessor.getState().isStarting()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
