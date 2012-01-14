package ru.tolmachev.parsing.conjunctive.stack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.tolmachev.core.IAbstractSymbol;
import ru.tolmachev.core.BGTableState;

import com.googlecode.alvor.sqlparser.IAction;
import com.googlecode.alvor.sqlparser.IParserState;

/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 04.12.11
 * Time: 19:21
 */

public class GraphStackNode implements IParserState {

    // state of table
    private BGTableState state;

    // position in input string between 0 and |w| - 1 for terminals
    private int position;

    private Map<GraphStackNode, IAbstractSymbol> successors = new HashMap<GraphStackNode, IAbstractSymbol>();

    private List<GraphStackNode> predecessors = new LinkedList<GraphStackNode>();

    public GraphStackNode(BGTableState state, int position) {
        this.state = state;
        this.position = position;
    }

    public boolean isTerminating() {
        return state.isAccepting();
    }

    public boolean isError() {
        return state.isError();
    }

    public Collection<IAction> getActions(int symbolNumber){
        return state.getActions(symbolNumber);
    }

    public Set<GraphStackNode> getAllPredecessorsWithGivenStepsBack(int step) {
        Set<GraphStackNode> predecessors = new HashSet<GraphStackNode>();
        predecessors.add(this);

        for (int currentStep = 0; currentStep < step; currentStep++) {
            Set<GraphStackNode> oneStepBackNodesSet = new HashSet<GraphStackNode>();

            for (GraphStackNode n : predecessors) {
                oneStepBackNodesSet.addAll(n.getPredecessors());
            }
            predecessors = oneStepBackNodesSet;
        }
        return predecessors;
    }

    public BGTableState getState() {
        return state;
    }

    public int getPosition() {
        return position;
    }

    public void addPredecessor(GraphStackNode predecessor) {
        predecessors.add(predecessor);
    }

    public void addSuccessor(IAbstractSymbol grammarElement, GraphStackNode successor) {
        successors.put(successor, grammarElement);
    }

    public void removeSuccessor(GraphStackNode successor) {
        successors.remove(successor);
    }

    public List<GraphStackNode> getPredecessors() {
        return predecessors;
    }

    public int getSuccessorsAmount() {
        return successors.size();
    }

    public int getPredecessorsAmount() {
        return predecessors.size();
    }

    public IAbstractSymbol getGrammarElementBySuccessor(GraphStackNode successor) {
        return successors.get(successor);
    }
}
