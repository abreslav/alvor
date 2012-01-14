package ru.tolmachev.parsing.bool;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.tolmachev.core.Conjunct;
import ru.tolmachev.core.IAbstractSymbol;
import ru.tolmachev.core.LRParserTable;
import ru.tolmachev.core.Rule;
import ru.tolmachev.core.BGTableState;
import ru.tolmachev.core.action.GotoAction;
import ru.tolmachev.core.action.ReduceAction;
import ru.tolmachev.core.action.ShiftAction;
import ru.tolmachev.core.grammar_elems.Nonterminal;
import ru.tolmachev.core.grammar_elems.Terminal;
import ru.tolmachev.parsing.AbstractInterpreter;
import ru.tolmachev.parsing.conjunctive.stack.Arc;
import ru.tolmachev.parsing.conjunctive.stack.GraphStack;
import ru.tolmachev.parsing.conjunctive.stack.GraphStackNode;
import ru.tolmachev.parsing.conjunctive.stack.GraphStackNode;
import ru.tolmachev.table.builder.enums.Punctuation;
import ru.tolmachev.table.builder.exceptions.UndefinedTerminalException;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.sqlparser.ErrorState;
import com.googlecode.alvor.sqlparser.ILRParser;
import com.googlecode.alvor.sqlparser.IParserState;

/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 22.12.11
 * Time: 17:12
 */

public class BooleanLRParser extends AbstractInterpreter implements ILRParser<GraphStack> {

    // graph stack
    //private GraphStack stack;

    private int pointer;

    private boolean parsingGoesOn;

    //private IAbstractInputItem[] inputString;

    private boolean secondRepeatFlag;

    public BooleanLRParser(final LRParserTable table) {
        super(table);
        initializeStartConditions();
    }

    void initializeStartConditions() {
        pointer = 0;
        parsingGoesOn = true;
    }

    public Map<String, Integer> getNamesToTokenNumbers() {
        return table.getNamesToTokenNumbers();
    }

    public Map<Integer, String> getSymbolNumbersToNames() {
        return table.getSymbolNumbersToNames();
    }

    public int getEofTokenIndex() {
        return Terminal.EOF.getCode();
    }

    public IParserState getInitialState() {
        BGTableState startState = table.getStartState();
        return new GraphStackNode(startState, 0);
    }

    @Override
    public GraphStack processToken(IAbstractInputItem token, int tokenIndex, GraphStack stack) {
        if (token.getCode() == IAbstractInputItem.EOF.getCode()) {
            return processEndOfStringToken(Terminal.EOF, tokenIndex, stack);
        } else {
        	Terminal terminal = new Terminal("For: " + token.toString() + "[" + token.getCode() + "]", tokenIndex);
            boolean reduceActionResult = makeReduceActions(terminal, stack);
            if (!reduceActionResult) {
            	return createErrorStack(null, token);
            }

            boolean shiftActionResult = makeShiftActions(terminal, stack);
            if (!shiftActionResult) {
                return createErrorStack(null, token);
            }

            return stack;
        }
    }

	private GraphStack createErrorStack(final GraphStackNode node, final IAbstractInputItem token) {
		return new GraphStack(new GraphStackNode(new BGTableState(-2), 0)) {
			public boolean hasErrorOnTop() {
				return true;
			};
			
			public IParserState getErrorOnTop() {
				return new ErrorState(node, token);
			};
		};
	}

    private GraphStack processEndOfStringToken(IAbstractSymbol token, int tokenIndex, GraphStack stack) {
        boolean reduceActionResult = makeReduceActions(token, stack);
        if (!reduceActionResult) {
            return null;
        }

        return stack;
    }

//    public boolean parse(final String input) {
//        initializeStartConditions();
//        try {
//            return parseString(input);
//        } catch (UndefinedTerminalException e) {
//            System.out.println(e.getMessage());
//            return false;
//        }
//    }

//    private boolean parseString(final String input) throws UndefinedTerminalException {
//        initializeStartConditions();
//        Terminal[] inputString = convertStringToWhalecalfParserTerminalsAsArray(input);
//        GraphStack graphStack = getStack();
//
//        for (int nI = 0; nI < inputString.length; nI++) {
//            IAbstractInputItem token = inputString[nI].getAbstractInputItem();
//            graphStack = processToken(token, inputString[nI].getCode(), graphStack);
//            if (graphStack == null) {
//                System.out.println("error at index: " + nI);
//                return false;
//            }
//        }
//
//        return checkForValidStackStructure(graphStack);
//    }


    private boolean makeShiftActions(IAbstractSymbol token, GraphStack stack) {
        if (!parsingGoesOn) {
            return false;
        }
        parsingGoesOn = false;

        Set<GraphStackNode> top = stack.getTop();
        // for all nodes at the top layer
        for (GraphStackNode node : top) {
            BGTableState state = node.getState();
            ShiftAction shift = state.getShift(token);

            // if shift available from current node with current terminal
            if (shift != null) {
                addNewNodeToTheFutureTopLayerByShift(stack, node, token, shift);
            } else {
                // if current node from current top layer has no arc to the future top layer - delete it
                stack.addNodeToGarbageCollector(node);
            }
        }

        // All nodes failed to shift this symbol. Return fail - unable to parse
        if (stack.isNewTopEmpty()) {
            return false;
        }

        parsingGoesOn = true;
        pointer++;
        // delete those nodes from which we can't get to the new top layer
        stack.clearGarbageCollector();
        // set new top for graph structured stack
        stack.updateTop();
        return true;
    }

    /**
     * method constructs next top layer - it adds new node to the future top layer by shift action from LR table
     *
     * @param stack    - stack
     * @param node     - a node in graph structured stack from the current top layer
     * @param terminal - current terminal
     * @param shift    - shift for pair (state in LR table, terminal)
     */
    private void addNewNodeToTheFutureTopLayerByShift(GraphStack stack, final GraphStackNode node, final IAbstractSymbol terminal, final ShiftAction shift) {
        //let's make new node in the future top layer
        BGTableState nextState = shift.getNextState();
        GraphStackNode previouslyCreatedNextNode = stack.findNodeInNextTop(nextState.getIndex());

        // if this node was previously created - connect it with current
        if (previouslyCreatedNextNode != null) {
            stack.connectNodes(node, previouslyCreatedNextNode, terminal);
            // else create it and connect with current node
        } else {
            int position = node.getPosition();
            GraphStackNode nextNode = new GraphStackNode(nextState, position + 1);
            stack.connectNodes(node, nextNode, terminal);

            stack.addNodeToNextTopLayer(nextNode);
            stack.addNode(nextNode);
        }
    }

    private boolean makeReduceActions(IAbstractSymbol token, GraphStack stack) {
        if (!parsingGoesOn) {
            return false;
        }
        parsingGoesOn = false;

        Map<Conjunct, Set<GraphStackNode>> conjunctsObtainedForNodes = initNodeMap();
        Map<Conjunct, Set<GraphStackNode>> conjunctsObtainedForNodesOnPreviousIteration = initNodeMap();

        boolean setOfConjunctsHasStabilized;
        for (; ; ) {
            if (table.isBooleanGrammar()) {
                conjunctsObtainedForNodesOnPreviousIteration = conjunctsObtainedForNodes;
                conjunctsObtainedForNodes = initNodeMap();
            }
            int newConjunctsAdded = 0;

            Set<GraphStackNode> topLayer = stack.getTop();
            for (GraphStackNode topNode : topLayer) {
                BGTableState state = topNode.getState();

                List<ReduceAction> reduceActionList = getReduceActions(state, token);
                if (reduceActionList == null) {
                    continue;
                }

                for (ReduceAction reduceAction : reduceActionList) {
                    boolean isModified = addStartNodesForReductionEdgeInMap(topNode, reduceAction, conjunctsObtainedForNodes);

                    // check if this reductions were made on the previous steps of infinite loop
                    if (isModified) {
                        newConjunctsAdded++;
                    }
                }
            }

            // if graph structured stack is stabilized - get out of infinite loop
            if (table.isBooleanGrammar()) {
                setOfConjunctsHasStabilized = isSetOfConjunctsStabilizedForBooleanGrammar(conjunctsObtainedForNodes, conjunctsObtainedForNodesOnPreviousIteration);
            } else {
                setOfConjunctsHasStabilized = isSetOfConjunctsStabilizedForConjunctiveGrammar(newConjunctsAdded);
            }

            if (setOfConjunctsHasStabilized) {
                break;
            }
            // flag
            secondRepeatFlag = false;

            Collection<Arc> arcsValidated = new LinkedList<Arc>();
            List<Rule> allRules = table.getRules();
            for (Rule rule : allRules) {
                Set<GraphStackNode> nodesThatMatchRule = getNodesOfStackMatchingRule(rule, conjunctsObtainedForNodes);

                for (GraphStackNode node : nodesThatMatchRule) {
                    BGTableState state = node.getState();
                    Nonterminal nonterminal = rule.getLeftPart();


                    GotoAction gotoAction = state.getGoto(nonterminal);
                    if (gotoAction == null) {
                        return false;
                    }
                    arcsValidated = addNewNodeToTheFutureTopLayerByReduction(stack, node, gotoAction, nonterminal, arcsValidated);
                }
            }

            if (table.isBooleanGrammar()) {
                makeInvalidation(stack, arcsValidated);
            }

            // no new reductions done.
            if (!secondRepeatFlag) {
                break;
            }
        }

        parsingGoesOn = true;
        return true;
    }

    /**
     * get reductions by state and position in the parsing string
     *
     * @param state - current state of LR table
     * @param token - current symbol in string
     * @return - list of reductions
     */
    private List<ReduceAction> getReduceActions(final BGTableState state, final IAbstractSymbol token) {
        if (token != Terminal.EOF) {
            return state.getReduceAction(token);
        } else {
            //get reduce action by empty string
            List<ReduceAction> list;
            try {
                Terminal epsilon = getEpsilonTerminal();
                list = state.getReduceAction(epsilon);
            } catch (UndefinedTerminalException e) {
                //System.out.println(" epsilon terminal is not defined! ");
                throw new RuntimeException("no epsilon symbol defined in table");
            }
            return list;
        }
    }

    private Terminal getEpsilonTerminal() throws UndefinedTerminalException {
        Punctuation epsilon = Punctuation.EPSILON;
        return (Terminal) table.getItemByStringValue(epsilon.getValue());
    }

    /**
     * for a node from stack, that has reduction(a conjunct) to make method calculates the start node for reduction edge
     * and puts it to map of (conjuncts, nodes)
     *
     * @param topNode                   - node of stack with reduction at his state
     * @param reduceAction              - reduction
     * @param conjunctsObtainedForNodes - map of conjuncts
     * @return true - if conjunctsObtainedForNodes has been modified, else false
     */
    private boolean addStartNodesForReductionEdgeInMap(final GraphStackNode topNode, final ReduceAction reduceAction, final Map<Conjunct, Set<GraphStackNode>> conjunctsObtainedForNodes) {
        Conjunct conjunct = reduceAction.getConjunct();
        // get all nodes where this
        // conjunct begins.
        int reductionLength = reduceAction.getLength();
        // get predecessors with k steps back
        Set<GraphStackNode> nodeWhereCurrentConjunctBegins = topNode.getAllPredecessorsWithGivenStepsBack(reductionLength);

        // add new nodes
        Set<GraphStackNode> conjunctForRule = conjunctsObtainedForNodes.get(conjunct);
        return conjunctForRule.addAll(nodeWhereCurrentConjunctBegins);
    }

    /**
     * add new node to the future top layer by reduction
     *
     * @param stack         - stack
     * @param node          - start point of reduction edge
     * @param gotoAction    - goto state from LR table
     * @param nonterminal   - left part of conjunct for current reduction
     * @param arcsValidated - arcs
     * @return true - if stack was modified, else false
     *         - if string is not valid
     */
    private Collection<Arc> addNewNodeToTheFutureTopLayerByReduction(final GraphStack stack, final GraphStackNode node, final GotoAction gotoAction, final Nonterminal nonterminal, final Collection<Arc> arcsValidated) {
        Set<GraphStackNode> top = stack.getTop();
        BGTableState nextState = gotoAction.getNextState();
        GraphStackNode previouslyCreatedNextNode = stack.findNodeInTop(nextState.getIndex());
        if (previouslyCreatedNextNode != null && stack.isNodesConnected(node, previouslyCreatedNextNode)) {
            if (table.isBooleanGrammar()) {
                Arc arc = new Arc(node, previouslyCreatedNextNode, nonterminal);
                arcsValidated.add(arc);
            }
        } else if (previouslyCreatedNextNode != null) {
            stack.connectNodes(node, previouslyCreatedNextNode, nonterminal);

            if (table.isBooleanGrammar()) {
                Arc arc = new Arc(node, previouslyCreatedNextNode, nonterminal);
                arcsValidated.add(arc);
            }
            secondRepeatFlag = true;
        } else {
            GraphStackNode newNode = new GraphStackNode(nextState, pointer);
            stack.connectNodes(node, newNode, nonterminal);

            top.add(newNode);
            stack.addNode(newNode);

            if (table.isBooleanGrammar()) {
                Arc arc = new Arc(node, newNode, nonterminal);
                arcsValidated.add(arc);
            }
            secondRepeatFlag = true;
        }
        return arcsValidated;
    }

    /**
     * as rule is A -> a1 & a2 & ... & an, get nodes that fits all conditions of a1 .. an
     *
     * @param rule                      - rule
     * @param conjunctsObtainedForNodes - map of nodes that matches conjuncts
     * @return nodes that matches rule
     */
    private Set<GraphStackNode> getNodesOfStackMatchingRule(final Rule rule, final Map<Conjunct, Set<GraphStackNode>> conjunctsObtainedForNodes) {
        int conjunctAmount = rule.getConjunctAmount();
        Conjunct firstConjunct = rule.getConjunctByIndex(0);
        Set<GraphStackNode> nodes = conjunctsObtainedForNodes.get(firstConjunct);
        Set<GraphStackNode> resultForRule = new HashSet<GraphStackNode>(nodes);

        for (int conjunctIndex = 1; conjunctIndex < conjunctAmount; conjunctIndex++) {
            Conjunct nextConjunct = rule.getConjunctByIndex(conjunctIndex);
            Set<GraphStackNode> next = conjunctsObtainedForNodes.get(nextConjunct);

            if (nextConjunct.getSign() == Conjunct.NEGATION) {
                resultForRule.removeAll(next);
            } else if (nextConjunct.getSign() == Conjunct.POSITIVE) {
                resultForRule.retainAll(next);
            }
        }
        return resultForRule;
    }

    private void makeInvalidation(GraphStack stack, Collection<Arc> arcsValidated) {
        boolean isArcsToInvalidate = false;

        Set<GraphStackNode> top = stack.getTop();
        for (GraphStackNode topNode : top) {
            List<GraphStackNode> predecessors = topNode.getPredecessors();

            Iterator<GraphStackNode> iterator = predecessors.iterator();
            while (iterator.hasNext()) {
                GraphStackNode predecessor = iterator.next();
                IAbstractSymbol grammarElement = predecessor.getGrammarElementBySuccessor(topNode);

                if (grammarElement instanceof Terminal) {
                    continue;
                }

                Arc arc = new Arc(predecessor, topNode, grammarElement);
                if (!arcsValidated.contains(arc)) {
                    stack.disconnectNodes(predecessor, topNode, iterator);
                    if (predecessor.getSuccessorsAmount() == 0) {
                        stack.addNodeToGarbageCollector(predecessor);
                    }
                    isArcsToInvalidate = true;
                }
            }
        }

        if (isArcsToInvalidate) {

            for (GraphStackNode topNode : top) {
                if (topNode.getSuccessorsAmount() == 0 && topNode.getPredecessorsAmount() == 0) {
                    stack.addNodeToGarbageCollector(topNode);
                } else {
                    stack.addNodeToNextTopLayer(topNode);
                }
            }

            stack.clearGarbageCollector();
            // set new top for graph structured stack
            stack.updateTop();
        }

        if (isArcsToInvalidate) {
            secondRepeatFlag = true;
        }
    }

    private Map<Conjunct, Set<GraphStackNode>> initNodeMap() {
        Map<Conjunct, Set<GraphStackNode>> map = new HashMap<Conjunct, Set<GraphStackNode>>();
        Collection<Conjunct> conjuncts = table.getConjuncts();
        for (Conjunct conjunct : conjuncts) {
            map.put(conjunct, new HashSet<GraphStackNode>());
        }
        return map;
    }

    private boolean isSetOfConjunctsStabilizedForBooleanGrammar(Map<Conjunct, Set<GraphStackNode>> conjunctsObtainedForNodesOnPreviousIteration,
                                                                Map<Conjunct, Set<GraphStackNode>> conjunctsObtainedForNodes) {
        return conjunctsObtainedForNodes.equals(conjunctsObtainedForNodesOnPreviousIteration);
    }

    private boolean isSetOfConjunctsStabilizedForConjunctiveGrammar(int newConjunctsAdded) {
        return newConjunctsAdded == 0;
    }


//    public static void main(String[] args) {
//        try {
//            TableBuilder tableBuilder = new TableBuilder(new File("trunk\\resources\\anbncn\\table.txt"));
//            LRParserTable table = tableBuilder.buildTable();
//            BooleanLRParser interpreter = new BooleanLRParser(table);
//            interpreter.parse("abc");
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (WrongInputFileStructureException e) {
//            e.printStackTrace();
//        }
//    }

}
