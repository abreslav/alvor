package ru.tolmachev.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ru.tolmachev.core.action.GotoAction;
import ru.tolmachev.core.action.ReduceAction;
import ru.tolmachev.core.action.ShiftAction;

import com.googlecode.alvor.sqlparser.IAction;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 18:51
 */


public class State {

    private int index;

    private List<Collection<IAction>> actionBySymbol = null;

    private boolean isTerminating;

    public State(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public ShiftAction getShift(IAbstractSymbol symbol) {
        Collection<IAction> actions = getActions(symbol.getCode());
        List<ShiftAction> shiftActions = new LinkedList<ShiftAction>();

        if (actions == null) {
            return null;
        }

        for (IAction action : actions) {
            if (action.consumes()) {
                shiftActions.add((ShiftAction) action);
            }
        }

        if (shiftActions.size() > 1) {
            throw new RuntimeException("there must not be shift/shift ambiguities");
        }

        if (shiftActions.isEmpty()) {
            return null;
        }
        return shiftActions.get(0);
    }

    public List<ReduceAction> getReduceAction(IAbstractSymbol symbol) {
        Collection<IAction> actions = getActions(symbol.getCode());
        List<ReduceAction> reduceActions = new LinkedList<ReduceAction>();

        if (actions == null) {
            return null;
        }

        for (IAction action : actions) {
            if (!action.consumes()) {
                reduceActions.add((ReduceAction) action);
            }
        }

        if (reduceActions.isEmpty()) {
            return null;
        }
        return reduceActions;
    }

    public GotoAction getGoto(IAbstractSymbol symbol) {
        Collection<IAction> actions = getActions(symbol.getCode());
        List<GotoAction> gotoActions = new LinkedList<GotoAction>();

        if (actions == null) {
            return null;
        }

        for (IAction action : actions) {
            gotoActions.add((GotoAction) action);
        }

        if (gotoActions.size() > 1) {
            throw new RuntimeException("there must not be goto/goto ambiguities");
        }

        if (gotoActions.isEmpty()) {
            return null;
        }
        return gotoActions.get(0);
    }

    public Collection<IAction> getActions(int symbolNumber) {
        if (symbolNumber < 0) {
            return null;
        }
        if (actionBySymbol == null) {
            return null;
        }
        if (symbolNumber >= actionBySymbol.size()) {
            return null;
        }
        Collection<IAction> actions = actionBySymbol.get(symbolNumber);
        if (actions == null) {
            return null;
        }
        return actions;
    }

    public void addAction(Integer symbolNumber, IAction action) {
        if (symbolNumber >= 0) {
            if (actionBySymbol == null) {
                actionBySymbol = new ArrayList<Collection<IAction>>();
            }
            if (actionBySymbol.size() <= symbolNumber) {
                addTo(actionBySymbol, symbolNumber, null);
            }
            Collection<IAction> collection = actionBySymbol.get(symbolNumber);
            if (collection == null) {
                collection = new ArrayList<IAction>();
                actionBySymbol.set(symbolNumber, collection);
            }
            collection.add(action);
        }
    }

    public void setTerminating(boolean terminating) {
        isTerminating = terminating;
    }

    public boolean isTerminating() {
        return isTerminating;
    }

    public boolean isError() {
        return false;
    }

    private static <T> void addTo(final List<T> list, int number, T element) {
        while (list.size() <= number) {
            list.add(null);
        }
        list.set(number, element);
    }

    @Override
    public String toString() {
        return String.valueOf(index);
    }
}
