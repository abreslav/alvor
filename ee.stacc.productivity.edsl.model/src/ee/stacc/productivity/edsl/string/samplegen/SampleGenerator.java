package ee.stacc.productivity.edsl.string.samplegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRandomInteger;
import ee.stacc.productivity.edsl.string.StringSequence;

public class SampleGenerator {
	private static Set<Character> intCharset = 
		new HashSet<Character>(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));
	
	public static List<String> getConcreteStrings(IAbstractString aStr) {
		List<String> result = new ArrayList<String>();
		
		if (aStr instanceof StringConstant) {
			result.add(((StringConstant)aStr).getConstant());
		}
		else if (aStr instanceof StringChoice) {
			for (IAbstractString item: ((StringChoice)aStr).getItems()) {
				result.addAll(getConcreteStrings(item));
			}
		}
		else if (aStr instanceof StringSequence) {
			
			for (IAbstractString item: ((StringSequence)aStr).getItems()) {
				if (result.isEmpty()) { // first piece
					result.addAll(getConcreteStrings(item));
				}
				else { // later parts should be multiplied by existing strings
					List<String> options = getConcreteStrings(item);
					List<String> tempList = new ArrayList<String>();
					for (String resultItem: result) {
						for (String option: options) {
							tempList.add(resultItem + option);
						}
					}
					result = tempList;
				}
			}
		}
		else if (aStr instanceof StringRandomInteger) {
			result.add(((StringRandomInteger)aStr).getExample());
		}
		else if (aStr instanceof StringCharacterSet) {
			// FIXME incorrect shortcut solution
			
			result.add("555");
			
			
//			StringCharacterSet cs = (StringCharacterSet)aStr;
//			if (cs.getContents().containsAll(intCharset) // gives NullPointerException
//					&& intCharset.containsAll(cs.getContents())) {
//				result.add("666");
//			}
//			else {
//				// creates nonvalid SQL
//				result.add(cs.toString());
//			}
		}
		else {
			throw new UnsupportedOperationException("getConcreteStrings, class="
					+ aStr.getClass());
		}
		return result;
	}
}
