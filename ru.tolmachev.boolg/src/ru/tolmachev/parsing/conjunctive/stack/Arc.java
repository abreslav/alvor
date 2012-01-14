package ru.tolmachev.parsing.conjunctive.stack;

import ru.tolmachev.core.IAbstractSymbol;


/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 09.12.11
 * Time: 11:01
 */

public class Arc {

    private final Node from;

    private final Node to;

    private final IAbstractSymbol symbol;

    public Arc(Node from, Node to, IAbstractSymbol symbol) {
        this.from = from;
        this.to = to;
        this.symbol = symbol;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public IAbstractSymbol getSymbol() {
        return symbol;
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }

        if (!(o instanceof Arc)) {
            return false;
        }

        Arc that = ((Arc) o);
        return (that.getFrom() == this.from) && (that.getTo() == this.to) && (that.getSymbol().equals(this.symbol));
    }
}
