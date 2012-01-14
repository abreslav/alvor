package ru.tolmachev.core.grammar_elems;

import ru.tolmachev.core.IAbstractSymbol;


/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 04.12.11
 * Time: 19:31
 */

public abstract class GrammarElement implements IAbstractSymbol {

    protected final String value;

    protected final int code;

    public GrammarElement(String value, int code) {
        this.value = value;
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return value;
    }

    public abstract boolean isTerminal();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GrammarElement)) {
            return false;
        }

        if (o instanceof Nonterminal) {
            return value.equals(((Nonterminal) o).getValue());
        }

        if (o instanceof Terminal) {
            return value.equals(((Terminal) o).getValue());
        }

        return false;
    }
}
