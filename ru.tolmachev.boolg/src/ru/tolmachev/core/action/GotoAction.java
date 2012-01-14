package ru.tolmachev.core.action;

import ru.tolmachev.core.State;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.sqlparser.IParserStack;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 18:40
 */

public class GotoAction extends AbstractAction {

    private final State nextState;

    public GotoAction(State nextState) {
        this.nextState = nextState;
    }

    public State getNextState() {
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
