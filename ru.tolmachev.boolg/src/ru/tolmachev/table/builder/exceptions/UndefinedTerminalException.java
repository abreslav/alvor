package ru.tolmachev.table.builder.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 27.11.11
 * Time: 0:38
 */


public class UndefinedTerminalException extends Exception {
    public UndefinedTerminalException() {
    }

    public UndefinedTerminalException(String message) {
        super(message);
    }

    public UndefinedTerminalException(String message, Throwable cause) {
        super(message, cause);
    }

    public UndefinedTerminalException(Throwable cause) {
        super(cause);
    }

}
