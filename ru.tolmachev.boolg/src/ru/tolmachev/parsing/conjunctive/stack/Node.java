package ru.tolmachev.parsing.conjunctive.stack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.tolmachev.core.IAbstractSymbol;
import ru.tolmachev.core.BGState;

import com.googlecode.alvor.sqlparser.IAction;
import com.googlecode.alvor.sqlparser.IParserState;

/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 04.12.11
 * Time: 19:21
 */

public class Node implements IParserState {

    // state of table
    private BGState state;

    // position in input string between 0 and |w| - 1 for terminals
    private int position;

    private Map<Node, IAbstractSymbol> successors = new HashMap<Node, IAbstractSymbol>();

    private List<Node> predecessors = new LinkedList<Node>();

    public Node(BGState state, int position) {
        this.state = state;
        this.position = position;
    }

    public boolean isTerminating() {
        return state.isTerminating();
    }

    public boolean isError() {
        return state.isError();
    }

    public Collection<IAction> getActions(int symbolNumber){
        return state.getActions(symbolNumber);
    }

    public Set<Node> getAllPredecessorsWithGivenStepsBack(int step) {
        Set<Node> predecessors = new HashSet<Node>();
        predecessors.add(this);

        for (int currentStep = 0; currentStep < step; currentStep++) {
            Set<Node> oneStepBackNodesSet = new HashSet<Node>();

            for (Node n : predecessors) {
                oneStepBackNodesSet.addAll(n.getPredecessors());
            }
            predecessors = oneStepBackNodesSet;
        }
        return predecessors;
    }

    public BGState getState() {
        return state;
    }

    public int getPosition() {
        return position;
    }

    public void addPredecessor(Node predecessor) {
        predecessors.add(predecessor);
    }

    public void addSuccessor(IAbstractSymbol grammarElement, Node successor) {
        successors.put(successor, grammarElement);
    }

    public void removeSuccessor(Node successor) {
        successors.remove(successor);
    }

    public List<Node> getPredecessors() {
        return predecessors;
    }

    public int getSuccessorsAmount() {
        return successors.size();
    }

    public int getPredecessorsAmount() {
        return predecessors.size();
    }

    public IAbstractSymbol getGrammarElementBySuccessor(Node successor) {
        return successors.get(successor);
    }
}
