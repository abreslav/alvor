package ee.stacc.productivity.edsl.sqlparser;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class LRParserLoader {

	@SuppressWarnings("unchecked")
	public static void load(String fileName, ILRParserBuilder builder) throws JDOMException, IOException {
		Document document = new SAXBuilder().build(fileName);
		Element root = document.getRootElement();
		Element grammar = root.getChild("grammar");
		List nonterminals = grammar.getChild("nonterminals").getChildren();
		for (Iterator iterator = nonterminals.iterator(); iterator.hasNext();) {
			Element terminal = (Element) iterator.next();
			int symbolNumber = Integer.parseInt(terminal.getAttributeValue("symbol-number"));
			String name = terminal.getAttributeValue("name");
			String usefulness = terminal.getAttributeValue("usefulness");
			use(usefulness);
			builder.addNonterminal(symbolNumber, name);
		}
		
		List rules = grammar.getChild("rules").getChildren("rule");
		for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
			Element rule = (Element) iterator.next();
			int number = Integer.parseInt(rule.getAttributeValue("number"));
			String usefulness = rule.getAttributeValue("usfulness");
			use(usefulness);
			String lhs = rule.getChild("lhs").getTextTrim();
			StringBuilder text = new StringBuilder(lhs).append(" -> ");
			List rhsSymbols = rule.getChild("rhs").getChildren("symbol");
			for (Iterator it = rhsSymbols.iterator(); it
					.hasNext();) {
				Element symbol = (Element) it.next();
				text.append(symbol.getTextTrim()).append(" ");
			}
			int rhsLength = rhsSymbols.size();
			builder.addRule(number, lhs, rhsLength, text.toString());
		}
		
		List terminals = grammar.getChild("terminals").getChildren();
		for (Iterator iterator = terminals.iterator(); iterator.hasNext();) {
			Element terminal = (Element) iterator.next();
			int symbolNumber = Integer.parseInt(terminal.getAttributeValue("symbol-number"));
			int tokenNumber = Integer.parseInt(terminal.getAttributeValue("token-number"));
			String name = terminal.getAttributeValue("name");
			String usefulness = terminal.getAttributeValue("usefulness");
			use(usefulness);
			builder.addTerminal(symbolNumber, tokenNumber, name);
		}
		
		Element automaton = root.getChild("automaton");
		
		List states = automaton.getChildren("state");
		for (Iterator iterator = states.iterator(); iterator.hasNext();) {
			Element state = (Element) iterator.next();
			int number = Integer.parseInt(state.getAttributeValue("number"));
			builder.createState(number);
		}

		for (Iterator iterator = states.iterator(); iterator.hasNext();) {
			Element state = (Element) iterator.next();
			int stateNumber = Integer.parseInt(state.getAttributeValue("number"));

			Element actions = state.getChild("actions");
			List transitions = actions.getChild("transitions").getChildren("transition");
			for (Iterator it = transitions.iterator(); it
					.hasNext();) {
				Element transition = (Element) it.next();
				String type = transition.getAttributeValue("type");
				String symbol = transition.getAttributeValue("symbol");
				int toState = Integer.parseInt(transition.getAttributeValue("state"));
				if ("goto".equals(type)) {
					builder.addGotoAction(stateNumber, symbol, toState);
				} else if ("shift".equals(type)) {
					builder.addShiftAction(stateNumber, symbol, toState);
				} else {
					throw new UnsupportedOperationException("Unknown type: " + type);
				}
			}
			
			List reductions = actions.getChild("reductions").getChildren("reduction");
			for (Iterator it = reductions.iterator(); it
					.hasNext();) {
				Element reduction = (Element) it.next();
				String symbol = reduction.getAttributeValue("symbol");
				String rule = reduction.getAttributeValue("rule");
				String enabled = reduction.getAttributeValue("enabled");
				use(enabled);
				if ("accept".equals(rule)) {
					builder.addAcceptAction(stateNumber, symbol);
				} else {
					int ruleNumber = Integer.parseInt(rule);
					builder.addReduceAction(stateNumber, symbol, ruleNumber);
				}
			}
			
			if (actions.getChild("errors").getChildren("error").size() > 0) {
				throw new UnsupportedOperationException("Errors are not supported");
			}
			
			if (state.getChild("solved-conflicts").getChildren().size() > 0) {
				throw new UnsupportedOperationException("Conflicts are not supported");
			}
		}
		
	}

	private static void use(Object value) {
		System.out.println("Unused value: " + value);
	}

}
