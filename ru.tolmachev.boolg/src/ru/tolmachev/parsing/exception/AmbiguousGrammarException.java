package ru.tolmachev.parsing.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 27.11.11
 * Time: 1:20
 */

public class AmbiguousGrammarException extends Exception {
    public AmbiguousGrammarException() {
    }

    public AmbiguousGrammarException(String message) {
        super(message);
    }

    public AmbiguousGrammarException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmbiguousGrammarException(Throwable cause) {
        super(cause);
    }

}
