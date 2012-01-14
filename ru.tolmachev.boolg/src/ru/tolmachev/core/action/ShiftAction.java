package ru.tolmachev.core.action;

import ru.tolmachev.core.BGTableState;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.sqlparser.IParserStack;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 18:36
 */

public class ShiftAction extends AbstractAction {

    private final BGTableState nextState;

    public ShiftAction(BGTableState nextState) {
        this.nextState = nextState;
    }

    public BGTableState getNextState() {
        return nextState;
    }

    public boolean isError() {
        return false;
    }

    public boolean consumes() {
        return true;
    }

    public IParserStack process(IAbstractInputItem inputItem, IParserStack stack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "ShiftAction: next state " + nextState;
    }
}
