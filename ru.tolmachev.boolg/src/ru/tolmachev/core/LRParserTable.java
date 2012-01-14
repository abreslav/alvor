package ru.tolmachev.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.tolmachev.core.action.GotoAction;
import ru.tolmachev.core.grammar_elems.Nonterminal;
import ru.tolmachev.table.builder.exceptions.UndefinedTerminalException;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 12:42
 */

public class LRParserTable {

    // list of terminal and nonterminals
    private List<IAbstractSymbol> symbols = new LinkedList<IAbstractSymbol>();

    private Map<String, IAbstractSymbol> namesToTokens = new HashMap<String, IAbstractSymbol>();

    private Nonterminal startNonterminal;

    private List<Rule> rules = new LinkedList<Rule>();

    //private Map<Integer, State> states;
    private List<State> states;

    private Map<String, Integer> namesToSymbolNumbers = new HashMap<String, Integer>();

    private Map<String, Integer> namesToTokenNumbers = new HashMap<String, Integer>();

    private Map<Integer, String> symbolNumbersToNames = new HashMap<Integer, String>();

    private State acceptingState;

    private boolean isSomeRuleContainsNegativeConjunct;

    public LRParserTable() {
        this.states = new ArrayList<State>();
    }

    public State getStateById(int id) {
        return states.get(id);
    }

    public List<State> getStates() {
        return states;
    }

    public boolean isBooleanGrammar() {
        return isSomeRuleContainsNegativeConjunct;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
        this.isSomeRuleContainsNegativeConjunct = checkForNegativeConjuncts(rules);
    }

    public List<Rule> getRules() {
        return rules;
    }

    public State getStartState() {
        return states.get(0);
    }

    public void setAcceptingState(State acceptingState) {
        this.acceptingState = acceptingState;
    }

    public State getAcceptingState() {
        return acceptingState;
    }

    public void setSymbols(List<IAbstractSymbol> inputItems) {
        this.symbols = inputItems;
    }

    public void setStartNonterminal(Nonterminal startNonterminal) {
        this.startNonterminal = startNonterminal;
    }

    public Map<String, IAbstractSymbol> getNamesToTokens() {
        return namesToTokens;
    }

    public void setStringToItemMap(Map<String, IAbstractSymbol> stringToItemMap) {
        this.namesToTokens = stringToItemMap;
    }

    public void setNamesToSymbolNumbers(Map<String, Integer> namesToSymbolNumbers) {
        this.namesToSymbolNumbers = namesToSymbolNumbers;
    }

    public void setNamesToTokenNumbers(Map<String, Integer> namesToTokenNumbers) {
        this.namesToTokenNumbers = namesToTokenNumbers;
    }

    public Map<Integer, String> getSymbolNumbersToNames() {
        return symbolNumbersToNames;
    }

    public void setSymbolNumbersToNames(Map<Integer, String> symbolNumbersToNames) {
        this.symbolNumbersToNames = symbolNumbersToNames;
    }

    public Map<String, Integer> getNamesToSymbolNumbers() {
        return namesToSymbolNumbers;
    }

    public Map<String, Integer> getNamesToTokenNumbers() {
        return namesToTokenNumbers;
    }

    public IAbstractSymbol getItemByStringValue(String value) throws UndefinedTerminalException {
        if (!namesToTokens.containsKey(value)) {
            throw new UndefinedTerminalException("no terminal with value: " + value);
        }

        return namesToTokens.get(value);
    }

    public void addState(State state) {
        addTo(states, state.getIndex(), state);
    }

    public State getState(int number) {
        if (states.size() <= number) {
            return null;
        }

        return states.get(number);
    }

    /**
     * calculates accepting state of LR table
     *
     * @return - accepting state of LR table
     */
    public State calculateAcceptingState() {
        State startState = states.get(0);
        GotoAction gotoAction = startState.getGoto(startNonterminal);
        if (gotoAction == null) {
            throw new RuntimeException("no accepting state");
        }

        State acceptigState = gotoAction.getNextState();
        acceptigState.setTerminating(true);
        return acceptigState;
    }

    public Collection<Conjunct> getConjuncts() {
        Collection<Conjunct> conjuncts = new HashSet<Conjunct>();
        for (Rule rule : rules) {
            List<Conjunct> c = rule.getConjuncts();
            conjuncts.addAll(c);
        }
        return conjuncts;
    }

    private boolean checkForNegativeConjuncts(Collection<Rule> rules) {
        for (Rule rule : rules) {
            List<Conjunct> conjuncts = rule.getConjuncts();
            for (Conjunct conjunct : conjuncts) {
                if (conjunct.getSign() == Conjunct.NEGATION) {
                    return true;
                }
            }
        }
        return false;
    }

    private static <T> void addTo(final List<T> list, int number, T element) {
        while (list.size() <= number) {
            list.add(null);
        }
        list.set(number, element);
    }
}
