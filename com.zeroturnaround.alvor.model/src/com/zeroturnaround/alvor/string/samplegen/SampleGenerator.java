package com.zeroturnaround.alvor.string.samplegen;

import java.util.ArrayList;
import java.util.List;

import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringRandomInteger;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;

public class SampleGenerator {
//	private static Set<Character> intCharset = 
//		new HashSet<Character>(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));
	
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
			result.add("123");
		}
		else if (aStr instanceof StringCharacterSet) {
			StringCharacterSet cs = (StringCharacterSet)aStr;
			// take only one character
			if (cs.getContents().iterator().hasNext()) {
				result.add(cs.getContents().iterator().next().toString());
			}
		}
		else if (aStr instanceof StringRepetition) {
			return getConcreteStrings(repetitionToChoice((StringRepetition)aStr));
		}
		else {
			throw new UnsupportedOperationException("getConcreteStrings, class="
					+ aStr.getClass());
		}
		return result;
	}
	
	private static IAbstractString repetitionToChoice(StringRepetition str) {
		return new StringChoice(str.getBody(),
				new StringSequence(str.getBody(), str.getBody())
				);
	}
}
