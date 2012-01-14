package ru.tolmachev.table.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ru.tolmachev.core.Conjunct;
import ru.tolmachev.core.IAbstractSymbol;
import ru.tolmachev.core.LRParserTable;
import ru.tolmachev.core.Rule;
import ru.tolmachev.core.BGTableState;
import ru.tolmachev.core.action.GotoAction;
import ru.tolmachev.core.action.ReduceAction;
import ru.tolmachev.core.action.ShiftAction;
import ru.tolmachev.core.grammar_elems.Nonterminal;
import ru.tolmachev.core.grammar_elems.Terminal;
import ru.tolmachev.table.builder.exceptions.WrongInputFileStructureException;

import com.googlecode.alvor.sqlparser.IAction;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 26.11.11
 * Time: 12:46
 */

public class TableBuilder {

    //scanner to scan file;
    private Scanner scanner;

    private static final String TERMINAL_BLOCK_START_TAG = "terminals";

    private static final String TERMINAL_BLOCK_STOP_TAG = "terminals_end";

    private static final String NON_TERMINAL_BLOCK_START_TAG = "non_terminals";

    private static final String NON_TERMINAL_BLOCK_STOP_TAG = "non_terminals_end";

    private static final String GRAMMAR_RULES_BLOCK_START_TAG = "grammar_rules";

    private static final String GRAMMAR_RULES_BLOCK_STOP_TAG = "grammar_rules_end";

    private static final String LR_TABLE_START_TAG = "LR_table";

    private static final String LR_TABLE_STOP_TAG = "LR_table_end";

    private static final String LR_TABLE_STATE_START_TAG = "state";

    private static final String LR_TABLE_STATE_STOP_TAG = "state_end";

    private static final String SHIFT = "shift";

    private static final String GOTO = "goto";

    private static final String REDUCTION = "reduction";

    private final Map<String, Conjunct> conjuncts = new HashMap<String, Conjunct>();

    private final List<IAbstractSymbol> symbols = new ArrayList<IAbstractSymbol>();

    private final Map<String, IAbstractSymbol> stringToInputItemMap = new HashMap<String, IAbstractSymbol>();

    private final Map<String, Integer> namesToSymbolNumbers = new HashMap<String, Integer>();

    private final Map<String, Integer> namesToTokenNumbers = new HashMap<String, Integer>();

    private final Map<Integer, String> symbolNumbersToNames = new HashMap<Integer, String>();

    private LRParserTable table = new LRParserTable();

    private int grammarElementsCounter = 0;

    public TableBuilder(File file) throws IOException {
        this.scanner = new Scanner(file);
    }

    public LRParserTable buildTable() throws WrongInputFileStructureException {
        if (!scanner.hasNextLine()) {
            throw new WrongInputFileStructureException("empty input file");
        }

        List<Terminal> terminalSet = getTerminals(scanner);
        symbols.addAll(terminalSet);

        List<Nonterminal> nonterminalList = getNonTerminals(scanner);
        symbols.addAll(nonterminalList);

        table.setSymbols(symbols);
        table.setStringToItemMap(stringToInputItemMap);
        table.setStartNonterminal(nonterminalList.iterator().next());

        table.setNamesToSymbolNumbers(namesToSymbolNumbers);
        table.setNamesToTokenNumbers(namesToTokenNumbers);

        List<Rule> rules = getGrammarRules(scanner);
        table.setRules(rules);

        table = getLRTable(scanner);
        return table;
    }

    private List<Terminal> getTerminals(final Scanner scanner) throws WrongInputFileStructureException {
        String line = scanner.nextLine();
        if (!line.equals(TERMINAL_BLOCK_START_TAG)) {
            throw new WrongInputFileStructureException("no terminal block start tag");
        }

        line = scanner.nextLine();
        List<Terminal> terminalList = new LinkedList<Terminal>();
        while (!TERMINAL_BLOCK_STOP_TAG.equals(line)) {
            Terminal terminal = new Terminal(line, grammarElementsCounter);
            terminalList.add(terminal);
            stringToInputItemMap.put(line, terminal);
            namesToSymbolNumbers.put(line, grammarElementsCounter);
            namesToTokenNumbers.put(line, grammarElementsCounter);
            symbolNumbersToNames.put(grammarElementsCounter, line);
            grammarElementsCounter++;
            line = scanner.nextLine();
        }
        namesToTokenNumbers.put(Terminal.EOF.getValue(), Terminal.EOF.getCode());

        if (terminalList.isEmpty()) {
            throw new WrongInputFileStructureException("no terminals in table description");
        }

        return terminalList;
    }

    private List<Nonterminal> getNonTerminals(final Scanner scanner) throws WrongInputFileStructureException {
        String line = scanner.nextLine();
        if (!line.equals(NON_TERMINAL_BLOCK_START_TAG)) {
            throw new WrongInputFileStructureException("no nonterminal block start tag");
        }

        line = scanner.nextLine();
        List<Nonterminal> nonterminalList = new LinkedList<Nonterminal>();
        while (!NON_TERMINAL_BLOCK_STOP_TAG.equals(line)) {
            Nonterminal nonterminal = new Nonterminal(line, grammarElementsCounter);
            nonterminalList.add(nonterminal);
            stringToInputItemMap.put(line, nonterminal);
            namesToSymbolNumbers.put(line, grammarElementsCounter);
            grammarElementsCounter++;
            line = scanner.nextLine();
        }

        if (nonterminalList.isEmpty()) {
            throw new WrongInputFileStructureException("no nonterminals in table description");
        }

        return nonterminalList;
    }

    private List<Rule> getGrammarRules(final Scanner scanner) throws WrongInputFileStructureException {
        String line = scanner.nextLine();
        if (!line.equals(GRAMMAR_RULES_BLOCK_START_TAG)) {
            throw new WrongInputFileStructureException("no grammar rules block start tag");
        }

        List<Rule> ruleList = new LinkedList<Rule>();
        line = scanner.nextLine();
        while (!GRAMMAR_RULES_BLOCK_STOP_TAG.equals(line)) {
            Nonterminal leftPart = getLeftPartOfTheRule(line);
            List<Conjunct> rightPart = getConjuncts(line);
            ruleList.add(new Rule(leftPart, rightPart));
            line = scanner.nextLine();
        }
        return ruleList;
    }

    private List<Conjunct> getConjuncts(String rule) throws WrongInputFileStructureException {
        Nonterminal leftPartOfConjunct = getLeftPartOfTheRule(rule);
        String rightPartOfTheRule = getRightPartOfTheRule(rule);
        String[] conjunctsMassive = rightPartOfTheRule.split("&");
        List<Conjunct> conjunctList = new LinkedList<Conjunct>();
        for (String rightPartOfConjunct : conjunctsMassive) {
            rightPartOfConjunct = rightPartOfConjunct.trim();
            int sign = getSign(rightPartOfConjunct);
            List<IAbstractSymbol> rightPartOfConjunctList = getRightPartOfConjunct(rightPartOfConjunct);
            Conjunct conjunct = new Conjunct(leftPartOfConjunct, rightPartOfConjunctList, sign);
            conjunctList.add(conjunct);
            conjuncts.put(conjunct.toString(), conjunct);
        }
        return conjunctList;
    }

    private int getSign(String rightPartOfConjunct) {
        if (rightPartOfConjunct.startsWith("~")) {
            return Conjunct.NEGATION;
        } else {
            return Conjunct.POSITIVE;
        }
    }

    private List<IAbstractSymbol> getRightPartOfConjunct(String rightPartOfConjunct) throws WrongInputFileStructureException {
        if (rightPartOfConjunct.startsWith("~")) {
            rightPartOfConjunct = rightPartOfConjunct.substring(1, rightPartOfConjunct.length());
        }

        String[] grammarElemsAsStr = rightPartOfConjunct.split(" ");
        List<IAbstractSymbol> rightPartOfConjunctList = new ArrayList<IAbstractSymbol>();

        for (String grammarElem : grammarElemsAsStr) {
            IAbstractSymbol item = stringToInputItemMap.get(grammarElem);
            if (item == null) {
                throw new WrongInputFileStructureException("undefined grammar elem: " + grammarElem);
            }
            rightPartOfConjunctList.add(item);
        }

        return rightPartOfConjunctList;
    }

    private Nonterminal getLeftPartOfTheRule(String rule) throws WrongInputFileStructureException {
        String[] ruleParts = rule.split("->");
        String leftPart = ruleParts[0];
        leftPart = leftPart.trim();
        IAbstractSymbol nonterminal = stringToInputItemMap.get(leftPart);

        if (nonterminal == null) {
            throw new WrongInputFileStructureException("undefined nonterminal: " + leftPart + " in rule: " + rule);
        }

        return (Nonterminal) nonterminal;
    }

    private String getRightPartOfTheRule(String rule) {
        String[] ruleParts = rule.split("->");
        String rightPart = ruleParts[1];
        rightPart = rightPart.trim();
        return rightPart;
    }

    private LRParserTable getLRTable(final Scanner scanner) throws WrongInputFileStructureException {
        String line = scanner.nextLine();
        if (!line.equals(LR_TABLE_START_TAG)) {
            throw new WrongInputFileStructureException("no LR table start tag");
        }

        line = scanner.nextLine();
        while (!LR_TABLE_STOP_TAG.equals(line)) {
            BGTableState state = getLRTableState(scanner, line);
            table.addState(state);
            line = scanner.nextLine();
        }

        BGTableState acceptingState = table.calculateAcceptingState();
        table.setAcceptingState(acceptingState);
        return table;
    }

    private BGTableState getLRTableState(final Scanner scanner, String line) throws WrongInputFileStructureException {
        int stateNumber;
        if (line.startsWith(LR_TABLE_STATE_START_TAG)) {
            stateNumber = getStateNumber(line);
        } else {
            throw new WrongInputFileStructureException("no LR table state block start tag");
        }
        BGTableState state = table.getState(stateNumber);
        if (state == null) {
            state = new BGTableState(stateNumber);
            table.addState(state);
        }

        line = scanner.nextLine();
        while (!LR_TABLE_STATE_STOP_TAG.equals(line)) {
            IAction action;
            IAbstractSymbol item;
            if (line.startsWith(SHIFT)) {
                item = parseItemInShiftAction(line);
                action = parseShiftAction(line);
            } else if (line.startsWith(GOTO)) {
                item = parseItemInGotoAction(line);
                action = parseGotoAction(line);
            } else if (line.startsWith(REDUCTION)) {
                item = parseItemInReduceAction(line);
                action = parseReduceAction(line);
            } else {
                throw new WrongInputFileStructureException("wrong structure of LR state table block!");
            }
            state.addAction(item.getCode(), action);
            line = scanner.nextLine();
        }
        return state;
    }

    private ShiftAction parseShiftAction(String str) throws WrongInputFileStructureException {
        String m[] = str.split(" ");
        if (m.length != 3) {
            throw new WrongInputFileStructureException("wrong strucure of shift command");
        }

        int nextStateIndex = Integer.valueOf(m[2]);
        BGTableState nextState = table.getState(nextStateIndex);
        if (nextState == null) {
            nextState = new BGTableState(nextStateIndex);
            table.addState(nextState);
        }

        return new ShiftAction(nextState);
    }

    private IAbstractSymbol parseItemInShiftAction(String str) throws WrongInputFileStructureException {
        String m[] = str.split(" ");
        if (m.length != 3) {
            throw new WrongInputFileStructureException("wrong strucure of shift command");
        }
        String terminalAsStr = m[1];

        IAbstractSymbol terminal = stringToInputItemMap.get(terminalAsStr);
        if (terminal == null) {
            throw new WrongInputFileStructureException("undefined terminal: " + terminalAsStr);
        }

        if (!terminal.isTerminal()) {
            throw new WrongInputFileStructureException("terminal: " + terminalAsStr + " defined as nonterminal");
        }

        return terminal;
    }

    private GotoAction parseGotoAction(String str) throws WrongInputFileStructureException {
        String m[] = str.split(" ");
        if (m.length != 3) {
            throw new WrongInputFileStructureException("wrong strucure of goto command");
        }

        int nextStateIndex = Integer.valueOf(m[2]);
        BGTableState nextState = table.getState(nextStateIndex);
        if (nextState == null) {
            nextState = new BGTableState(nextStateIndex);
            table.addState(nextState);
        }

        return new GotoAction(nextState);
    }

    private IAbstractSymbol parseItemInGotoAction(String str) throws WrongInputFileStructureException {
        String m[] = str.split(" ");
        if (m.length != 3) {
            throw new WrongInputFileStructureException("wrong strucure of goto command");
        }

        String nonterminalAsStr = m[1];

        IAbstractSymbol nonterminal = stringToInputItemMap.get(nonterminalAsStr);
        if (nonterminal == null) {
            throw new WrongInputFileStructureException("undefined nonterminal: " + nonterminalAsStr);
        }
        if (nonterminal.isTerminal()) {
            throw new WrongInputFileStructureException("nonterminal: " + nonterminalAsStr + " defined as terminal");
        }

        return nonterminal;
    }

    private ReduceAction parseReduceAction(String str) throws WrongInputFileStructureException {
        int index = str.indexOf(' ');
        String terminalAndRule = str.substring(index, str.length());
        terminalAndRule = terminalAndRule.trim();
        index = terminalAndRule.indexOf(' ');

        String ruleAsStr = terminalAndRule.substring(index, terminalAndRule.length());
        ruleAsStr = ruleAsStr.trim();

        Conjunct conjunct = getConjunctByStringValue(ruleAsStr);

        return new ReduceAction(conjunct);
    }

    private IAbstractSymbol parseItemInReduceAction(String str) throws WrongInputFileStructureException {
        int index = str.indexOf(' ');
        String terminalAndRule = str.substring(index, str.length());
        terminalAndRule = terminalAndRule.trim();
        index = terminalAndRule.indexOf(' ');
        String terminalAsStr = terminalAndRule.substring(0, index);
        terminalAsStr = terminalAsStr.trim();

        IAbstractSymbol terminal = stringToInputItemMap.get(terminalAsStr);
        if (terminal == null) {
            throw new WrongInputFileStructureException("undefined terminal: " + terminalAsStr);
        }

        if (!terminal.isTerminal()) {
            throw new WrongInputFileStructureException("terminal: " + terminalAsStr + " defined as nonterminal");
        }

        return terminal;
    }

    public Conjunct getConjunctByStringValue(String conjunct) {
        if (!conjuncts.containsKey(conjunct)) {
            throw new RuntimeException("fail with conjuncts" + conjunct);
        }

        return conjuncts.get(conjunct);
    }

    private int getStateNumber(String str) throws WrongInputFileStructureException {
        String m[] = str.split(" ");
        if (m.length != 2) {
            throw new WrongInputFileStructureException("wrong structure of state symbol");
        }

        return Integer.valueOf(m[1]);
    }

//    public static void main(String[] args) {
//        try {
//            TableBuilder tableBuilder = new TableBuilder(new File("resources\\arithmetic.txt"));
//            tableBuilder.buildTable();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (WrongInputFileStructureException e) {
//            e.printStackTrace();
//        }
//    }
    
    public static void main(String[] args) {
		for (char c = 'a'; c <= 'z'; c++) {
			makeToken(c);
		}
		for (char c = '0'; c <= '9'; c++) {
			makeToken(c);
		}
 	}

	private static void makeToken(char c) {
		System.out.println("%lex-token _" + c + " = \"" + c + "\"");
	}
}
