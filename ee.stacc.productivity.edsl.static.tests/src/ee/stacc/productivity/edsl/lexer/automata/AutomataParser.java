package ee.stacc.productivity.edsl.lexer.automata;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AutomataParser {

	public static State parse(String text) {
		text = text.replace('-', ' ');
		String[] lines = text.split(";");
		
		Map<String, State> map = new LinkedHashMap<String, State>();
		State initialState = null;
		for (String line : lines) {
			Pattern pattern = Pattern.compile("(!?[A-Za-z0-9]+)(:([A-Za-z0-9_\"]))?(/([A-Za-z0-9_]?))?");
			Matcher matcher = pattern.matcher(line);
			if (!matcher.find()) {
				continue;
			}
			
			State from = getState(map, matcher.group(1));
			if (initialState == null) {
				initialState = from;
			}
			while (matcher.find()) {
				State to = getState(map, matcher.group(1));
				char c = matcher.group(3).charAt(0);
				String outStr = matcher.group(5);
				Transition transition = new Transition(from, to, (int) c, outStr == null ? "" : outStr);
				from.getOutgoingTransitions().add(transition);
//				to.getIncomingTransitions().add(transition);
			}
		}
		return initialState;
	}
	
	private static State getState(Map<String, State> map, String name) {
		State state = map.get(name);
		if (state == null) {
			state = new State(name, name.startsWith("!"));
			map.put(name, state);
		}
		return state;
	}
}
