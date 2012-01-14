package ru.tolmachev.core.grammar_elems;

/**
 * Created by IntelliJ IDEA.
 * User: ִלטענטי
 * Date: 04.12.11
 * Time: 16:42
 */

public class Nonterminal extends GrammarElement {

    public Nonterminal(String value, int code) {
        super(value, code);
    }

    public boolean isTerminal() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Nonterminal)) {
            return false;
        }

        return value.equals(((Nonterminal) o).getValue());
    }

    public String toString() {
        return value;
    }
}
