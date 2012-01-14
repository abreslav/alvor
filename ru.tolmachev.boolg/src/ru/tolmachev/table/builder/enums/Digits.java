package ru.tolmachev.table.builder.enums;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 13:20
 */


/**
 * enum for real names of terminals-digits and values in whalecalf notation of grammar terminals by Ochotin's
 */
public enum Digits {

    ZERO  ("0", "_0"),
    ONE   ("1", "_1"),
    TWO   ("2", "_2"),
    THREE ("3", "_3"),
    FOUR  ("4", "_4"),
    FIVE  ("5", "_5"),
    SIX   ("6", "_6"),
    SEVEN ("7", "_7"),
    EIGHT ("8", "_8"),
    NINE  ("9", "_9");

    // real terminal value
    private String name;

    // terminal value in whale calf notation
    private String value;

    private Digits(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
