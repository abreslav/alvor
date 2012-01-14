package ru.tolmachev.table.builder.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 15:28
 */
public class WrongInputFileStructureException extends Exception {
    public WrongInputFileStructureException() {
    }

    public WrongInputFileStructureException(String message) {
        super(message);
    }

    public WrongInputFileStructureException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongInputFileStructureException(Throwable cause) {
        super(cause);
    }

}
