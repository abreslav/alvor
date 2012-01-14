package ru.tolmachev.table.builder.enums.container;

import java.util.HashMap;
import java.util.Map;

import ru.tolmachev.table.builder.enums.Digits;
import ru.tolmachev.table.builder.enums.Letters;
import ru.tolmachev.table.builder.enums.Punctuation;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 14:28
 */

public class TerminalSignContainer {

    private static TerminalSignContainer terminalContainer;

    private Map<String, String> map;

    public static TerminalSignContainer newInstance() {
        if (terminalContainer == null) {
            terminalContainer = new TerminalSignContainer();
        }
        return terminalContainer;
    }

    private TerminalSignContainer() {
        map = new HashMap<String, String>();
        map.putAll(createLetterMap());
        map.putAll(createDigitMap());
        map.putAll(createPunctuationMap());
    }

    /**
     * get nonterminal in Ochotin Whalecalf parser notation by usual terminal sign
     *
     * @param terminal - terminal sign
     * @return - terminal sign in Ochotin's notation
     */
    public String getByName(String terminal) {
        return map.get(terminal);
    }

    public boolean contains(String terminal) {
        return map.containsKey(terminal);
    }

    private Map<String, String> createLetterMap() {
        Map<String, String> map = new HashMap<String, String>();

        Letters letterSigns[] = Letters.values();
        for (Letters letter : letterSigns) {
            map.put(letter.getName(), letter.getValue());
        }
        return map;
    }

    private Map<String, String> createDigitMap() {
        Map<String, String> map = new HashMap<String, String>();

        Digits digitSigns[] = Digits.values();
        for (Digits digit : digitSigns) {
            map.put(digit.getName(), digit.getValue());
        }
        return map;
    }

    private Map<String, String> createPunctuationMap() {
        Map<String, String> map = new HashMap<String, String>();

        Punctuation punctuationSigns[] = Punctuation.values();
        for (Punctuation punctuation : punctuationSigns) {
            map.put(punctuation.getName(), punctuation.getValue());
        }
        return map;
    }

}
