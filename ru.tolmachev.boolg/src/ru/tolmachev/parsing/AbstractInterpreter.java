package ru.tolmachev.parsing;

import ru.tolmachev.core.IAbstractSymbol;
import ru.tolmachev.core.LRParserTable;
import ru.tolmachev.core.grammar_elems.Terminal;
import ru.tolmachev.table.builder.enums.container.TerminalSignContainer;
import ru.tolmachev.table.builder.exceptions.UndefinedTerminalException;

/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 04.12.11
 * Time: 21:05
 */

public abstract class AbstractInterpreter {

    //LR table with states
    protected final LRParserTable table;

    protected final TerminalSignContainer terminalSignContainer;

    protected AbstractInterpreter(LRParserTable table) {
        this.table = table;
        this.terminalSignContainer = TerminalSignContainer.newInstance();
    }

//    abstract public boolean parse(final String input);

    protected Terminal[] convertStringToWhalecalfParserTerminalsAsArray(final String input) throws UndefinedTerminalException {
        Terminal[] terminals = new Terminal[input.length() + 1];
        for (int currentIndex = 0; currentIndex < input.length(); currentIndex++) {
            char ch = input.charAt(currentIndex);
            String terminalInWhaleCalfNotation = getTerminalInWhalcalfParserStyle(ch);
            Terminal terminal = getTerminalByValue(terminalInWhaleCalfNotation);
            terminals[currentIndex] = terminal;
        }

        terminals[input.length()] = Terminal.EOF;
        return terminals;
    }

    private String getTerminalInWhalcalfParserStyle(final char ch) throws UndefinedTerminalException {
        String terminal = String.valueOf(ch);

        if (!terminalSignContainer.contains(terminal)) {
            System.out.println("wrong terminal: " + terminal);
            throw new UndefinedTerminalException("undefined terminal: " + ch);
        }
        return terminalSignContainer.getByName(terminal);
    }

    private Terminal getTerminalByValue(String value) throws UndefinedTerminalException {
        IAbstractSymbol item = table.getItemByStringValue(value);

        if(item == null){

        }
        
        if (!item.isTerminal()) {
            throw new UndefinedTerminalException("terminal " + value + " difined as nonterminal");
        }
        return (Terminal)item;
    }
}
