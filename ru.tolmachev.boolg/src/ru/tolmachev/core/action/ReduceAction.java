package ru.tolmachev.core.action;

import java.util.List;

import ru.tolmachev.core.Conjunct;
import ru.tolmachev.core.IAbstractSymbol;
import ru.tolmachev.core.grammar_elems.Terminal;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.sqlparser.IParserStack;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 18:48
 */

public class ReduceAction extends AbstractAction {

    private final Conjunct conjunct;

    // length of reduction
    private final int length;

    public ReduceAction(Conjunct conjunct) {
        this.conjunct = conjunct;
        this.length = getRightPartOfTheRuleLength();
    }

    public Conjunct getConjunct() {
        return conjunct;
    }

    public int getLength() {
        return length;
    }

    public int getRightPartOfTheRuleLength() {
        if (isEpsilon(conjunct.getRightPart())) {
            return 0;
        }

        return conjunct.getLength();
    }

    boolean isEpsilon(List<IAbstractSymbol> rightPartOfTheRule) {
        if (rightPartOfTheRule.size() != 1) {
            return false;
        }

        IAbstractSymbol item = rightPartOfTheRule.get(0);
        if (!item.isTerminal()) {
            return false;
        } else {
            Terminal terminal = (Terminal) item;
            return terminal.getValue().equals("_epsilon");
        }
    }

    public int hashCode() {
        return conjunct.hashCode();
    }

    public IParserStack process(IAbstractInputItem inputItem, IParserStack stack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "ReduceAction:";
    }
}
