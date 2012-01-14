package ru.tolmachev.core;

import java.util.Iterator;
import java.util.List;

import ru.tolmachev.core.grammar_elems.Nonterminal;

/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 05.12.11
 * Time: 19:28
 */

public class Conjunct {

    public static final int NEGATION = -1;

    public static final int POSITIVE = 1;

    // left part of conjunct
    private Nonterminal leftPart;

    // right part of conjunct
    private List<IAbstractSymbol> rightPart;

    // in boolean grammars conjunct may be positive or negative, in conjunctive - only positive
    private int sign;

    public Conjunct(Nonterminal leftPartOfConjunct, List<IAbstractSymbol> rightPartOfConjunct, int sign) {
        this.leftPart = leftPartOfConjunct;
        this.rightPart = rightPartOfConjunct;
        this.sign = sign;
    }

    public Nonterminal getLeftPart() {
        return leftPart;
    }

    public List<IAbstractSymbol> getRightPart() {
        return rightPart;
    }

    public int getSign() {
        return sign;
    }

    public int getLength() {
        return rightPart.size();
    }


    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Conjunct)) {
            return false;
        }

        Conjunct that = (Conjunct) o;
        return this.toString().equals(that.toString());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(leftPart.getValue());
        sb.append(" -> ");

        Iterator<IAbstractSymbol> iter = rightPart.iterator();
        while (iter.hasNext()) {
            IAbstractSymbol item = iter.next();
            sb.append(item.toString());

            if (iter.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
