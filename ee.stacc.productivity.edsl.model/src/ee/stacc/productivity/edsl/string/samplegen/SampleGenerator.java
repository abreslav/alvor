package ee.stacc.productivity.edsl.string.samplegen;

import java.util.ArrayList;
import java.util.List;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRandomInteger;
import ee.stacc.productivity.edsl.string.StringSequence;

public class SampleGenerator {
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
		else {
			throw new UnsupportedOperationException("getConcreteStrings, class="
					+ aStr.getClass());
		}
		return result;
	}
}
