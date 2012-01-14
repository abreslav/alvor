package ru.tolmachev.table.builder.enums;


/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 13:31
 */

public enum Punctuation {

    EPSILON("", "_epsilon"),
    SPACE(" ", "_space"),
    LEFTPAR("(", "_leftpar"),
    RIGHTPAR(")", "_rightpar"),
    LEFTBRACE("{", "_leftbrace"),
    RIGHTBRACE("}", "_rightbrace"),
    COMMA(",", "_comma"),
    SEMICOLON(";", "_semicolon"),
    PLUS("+", "_plus"),
    MINUS("-", "_minus"),
    STAR("*", "_star"),
    SLASH("/", "_slash"),
    PERCENT("%", "_percent"),
    AND("&", "_and"),
    OR("|", "_or"),
    EXCL("!", "_excl"),
    EQ("=", "_eq"),
    LT("<", "_lt"),
    GT(">", "_gt"),
    DOUBLE_QUOTE("\"", "_double_quote"),
    QUOTE("'", "_quote"),
    PERIOD(".", "_period"),
    COLON(":", "_colon"),
    QUESTION("?", "_question_mark"),
    UNDERSCORE("_", "_underscore"),
    LEFT_BRACKET("[", "_left_bracket"),
    RIGHT_BRACKET("]", "_right_bracket");

    // real terminal value
    private String name;

    // terminal value in whale calf notation
    private String value;

    private Punctuation(String name, String value) {
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
