package ru.tolmachev.parsing.conjunctive.stack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ru.tolmachev.core.IAbstractSymbol;
import ru.tolmachev.core.State;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.sqlparser.IParserStack;
import com.googlecode.alvor.sqlparser.IParserState;

/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 04.12.11
 * Time: 19:21
 */
public class GraphStack implements IParserStack {

    private Set<Node> top;

    private Set<Node> nextTop;

    private final List<Node> stack;

    private final Node startNode;

    private Set<Node> garbageCollection;

    public GraphStack(State start) {
        startNode = new Node(start, 0);
        stack = new LinkedList<Node>(Collections.singleton(startNode));
        top = new HashSet<Node>(Collections.singleton(startNode));
        nextTop = new HashSet<Node>();
        garbageCollection = new HashSet<Node>();
    }

    public Set<Node> getTop() {
        return top;
    }

    public void addNode(Node node) {
        stack.add(node);
    }

    public void addNodeToNextTopLayer(Node node) {
        nextTop.add(node);
    }

    public Node findNodeInTop(int stateIndex) {
        return findNodeByStateId(top, stateIndex);
    }

    public Node findNodeInNextTop(int stateIndex) {
        return findNodeByStateId(nextTop, stateIndex);
    }

    public Node findNodeByStateId(Set<Node> set, int stateIndex) {
        for (Node node : set) {
            State state = node.getState();
            if (state.getIndex() == stateIndex) {
                return node;
            }
        }
        return null;
    }

    public void addNodeToGarbageCollector(Node node) {
        garbageCollection.add(node);
    }

    public boolean isNewTopEmpty() {
        return nextTop.isEmpty();
    }

    public void updateTop() {
        top = nextTop;
        nextTop = new HashSet<Node>();
    }

    public void connectNodes(Node prev, Node next, IAbstractSymbol grammarElement) {
        prev.addSuccessor(grammarElement, next);
        next.addPredecessor(prev);
    }

    public void disconnectNodes(Node prev, Node next, Iterator<Node> iter) {
        prev.removeSuccessor(next);
        iter.remove();
    }


    public void clearGarbageCollector() {
        Set<Node> nodesWithoutSuccessors = getNodesWithoutSuccessors(garbageCollection);

        while (nodesWithoutSuccessors.size() > 0) {
            Iterator<Node> iter = nodesWithoutSuccessors.iterator();
            while (iter.hasNext()) {
                Node node = iter.next();
                if (cutBranch(node)) {
                    iter.remove();
                    stack.remove(node);
                    top.remove(node);
                }
            }
        }
        garbageCollection = new HashSet<Node>();
    }

    private Set<Node> getNodesWithoutSuccessors(Set<Node> setOfNodesToDelete) {
        Set<Node> nodesWithoutSuccessors = new HashSet<Node>();
        for (Node node : setOfNodesToDelete) {
            if (node.getSuccessorsAmount() == 0) {
                nodesWithoutSuccessors.add(node);
            }
        }
        return nodesWithoutSuccessors;
    }

    private boolean cutBranch(Node node) {
        if (node.getSuccessorsAmount() != 0) {
            return false;
        }

        List<Node> predecessors = node.getPredecessors();
        Iterator<Node> iter = predecessors.iterator();
        while (iter.hasNext()) {
            Node predecessor = iter.next();
            predecessor.removeSuccessor(node);
            cutBranch(predecessor);
            iter.remove();
            stack.remove(node);
            top.remove(node);
        }
        return true;
    }

    public boolean isNodesConnected(Node n1, Node n2) {
        List<Node> predecessors = n2.getPredecessors();
        for (Node predecessor : predecessors) {
            if (predecessor.equals(n1)) {
                return true;
            }
        }
        return false;
    }

    public Collection<IParserState> top() {
        Collection<IParserState> top = new HashSet<IParserState>();
        for (Node node : this.top) {
            top.add(node);
        }

        return top;
    }

    public IParserStack push(IParserState state) {
        throw new UnsupportedOperationException();
    }

    public IParserStack pop(int count) {
        throw new UnsupportedOperationException();
    }

    public boolean hasErrorOnTop() {
        return false;
    }

    public IParserState getErrorOnTop() {
        return null;
    }

    public boolean topAccepts() {
        return false;
    }
}
