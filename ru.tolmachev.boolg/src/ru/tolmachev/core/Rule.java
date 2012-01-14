package ru.tolmachev.core;

import java.util.List;

import ru.tolmachev.core.grammar_elems.Nonterminal;

/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 05.12.11
 * Time: 18:01
 */

public class Rule {

    private Nonterminal leftPart;

    private List<Conjunct> conjuncts;

    public Rule(Nonterminal leftPart, List<Conjunct> conjuncts) {
        this.leftPart = leftPart;
        this.conjuncts = conjuncts;
    }

    public List<Conjunct> getConjuncts() {
        return conjuncts;
    }

    public int getConjunctAmount(){
        return conjuncts.size();
    }

    public Nonterminal getLeftPart() {
        return leftPart;
    }

    public Conjunct getConjunctByIndex(int index) {
        return conjuncts.get(index);
    }
}
