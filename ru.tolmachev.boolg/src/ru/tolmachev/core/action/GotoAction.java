package ru.tolmachev.core.action;

import ru.tolmachev.core.BGState;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.sqlparser.IParserStack;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 18:40
 */

public class GotoAction extends AbstractAction {

    private final BGState nextState;

    public GotoAction(BGState nextState) {
        this.nextState = nextState;
    }

    public BGState getNextState() {
        return nextState;
    }

    public IParserStack process(IAbstractInputItem inputItem, IParserStack stack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "GotoAction: next state " + nextState;
    }
}
