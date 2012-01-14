package ru.tolmachev.parsing.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 27.11.11
 * Time: 12:38
 */

public class InvalidInputStringException extends Exception {

    public InvalidInputStringException() {
    }

    public InvalidInputStringException(String message) {
        super(message);
    }

    public InvalidInputStringException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInputStringException(Throwable cause) {
        super(cause);
    }

}
