package ru.tolmachev.table.builder.enums;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 13:09
 */


/**
 * enum for real names of terminals-characters and values in whalecalf notation of grammar terminals by Ochotin's
 */
public enum Letters {

    A("a", "_a"),
    B("b", "_b"),
    C("c", "_c"),
    D("d", "_d"),
    E("e", "_e"),
    F("f", "_f"),
    G("g", "_g"),
    H("h", "_h"),
    I("i", "_i"),
    J("j", "_j"),
    K("k", "_k"),
    L("l", "_l"),
    M("m", "_m"),
    N("n", "_n"),
    O("o", "_o"),
    P("p", "_p"),
    Q("q", "_q"),
    R("r", "_r"),
    S("s", "_s"),
    T("t", "_t"),
    U("u", "_u"),
    V("v", "_v"),
    W("w", "_w"),
    X("x", "_x"),
    Y("y", "_y"),
    Z("z", "_z");

    // real terminal value
    private String name;

    // terminal value in whale calf notation
    private String value;

    private Letters(String name, String value) {
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
