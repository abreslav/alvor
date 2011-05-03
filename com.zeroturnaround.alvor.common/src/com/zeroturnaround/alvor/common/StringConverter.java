package com.zeroturnaround.alvor.common;

import java.util.ArrayList;
import java.util.List;

import com.zeroturnaround.alvor.string.AbstractStringCollection;
import com.zeroturnaround.alvor.string.AbstractStringEqualsVisitor;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRecursion;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;

public class StringConverter {
	private static IAbstractStringVisitor<Boolean, IAbstractString> EQUALS_VISITOR = new AbstractStringEqualsVisitor(false);
	
	
	public static boolean includesStringExtensions(IAbstractString str) {
		if (str instanceof StringConstant) {
			return false;
		}
		else if (str instanceof StringCharacterSet) {
			return false;
		}
		else if (str instanceof StringParameter) {
			return false;
		}
		else if (str instanceof StringRecursion) {
			return true;
		}
		else if (str instanceof StringRepetition) {
			return includesStringExtensions(((StringRepetition)str).getBody());
		}
		else if (str instanceof AbstractStringCollection) {
			for (IAbstractString as : ((AbstractStringCollection)str).getItems()) {
				if (includesStringExtensions(as)) {
					return true;
				}
			}
			return false;
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	/**
	 * Transforms sequence of sequences to a flat sequence 
	 * and same for choices 
	 */
	public static IAbstractString flattenStringCollections(IAbstractString str) {
		if (str instanceof StringSequence) {
			List<IAbstractString> items = ((StringSequence)str).getItems();
			List<IAbstractString> result = new ArrayList<IAbstractString>();
			for (IAbstractString item : items) {
				IAbstractString flatItem = flattenStringCollections(item);
				if (flatItem instanceof StringSequence) {
					result.addAll(((StringSequence)flatItem).getItems());
				}
				else {
					result.add(flatItem);
				}
			}
			return new StringSequence(str.getPosition(), result);
		}
		
		else if (str instanceof StringChoice) {
			List<IAbstractString> items = ((StringChoice)str).getItems();
			List<IAbstractString> result = new ArrayList<IAbstractString>();
			for (IAbstractString item : items) {
				IAbstractString flatItem = flattenStringCollections(item);
				if (flatItem instanceof StringChoice) {
					result.addAll(((StringChoice)flatItem).getItems());
				}
				else {
					result.add(flatItem);
				}
			}
			return new StringChoice(str.getPosition(), result);
		}
		
		else if (str instanceof StringRepetition) {
			StringRepetition rep = (StringRepetition)str;
			return new StringRepetition(rep.getPosition(),  
					flattenStringCollections(((StringRepetition)str).getBody()));
		}
		else {
			return str;
		}
	}
	
	/**
	 * Optimizes choice of sequences that have same head
	 * eg. transforms (a + b | a + c) into a + (b | c)
	 * 
	 * NB! it's nonrecursive - optimizes only outermost Choice
	 * 
	 * 3 schemes are considered separately
	 *    (a + b | a) 				-> a + (b | _)
	 *    (a | a + b) 				-> a + (b | _)          
	 *    (a + b | a + c | ...) 	-> a + (b | c | ...)
	 */
	public static IAbstractString optimizeChoice(StringChoice str) {
		if (str.getItems().size() < 2) {
			return str;
		}
		
		if (str.getItems().size() == 2) {
			// first assume 2nd item is also prefix for 1st
			IAbstractString commonHead = str.get(1);
			IAbstractString containingStr = str.get(0);
			IAbstractString tail = getTailIfHasHead(containingStr, commonHead);
			
			if (tail == null) {
				// try other way around
				commonHead = str.get(0);
				containingStr = str.get(1);
				tail = getTailIfHasHead(containingStr, commonHead);
			}
			
			// if either way succeeded then construct sequence of choice
			if (tail != null) {
				return new StringSequence(
						str.getPosition(), 
						commonHead, 
						new StringChoice(
								containingStr.getPosition(), 
								tail, 
								// FIXME this position is not good
								new EmptyStringConstant()));
			}
			else {
				return str;
			}
		}
		else {
			return str;
		}
	}
	
	private static IAbstractString getTailIfHasHead(IAbstractString str, 
			IAbstractString head) {
		if (str instanceof StringSequence) {
			StringSequence seq = (StringSequence)str;
			if (seq.getItems().isEmpty()) {
				return null;
			}
			
			if (str.containsRecursion()) {
				System.out.println("REC: " + str);
			}
			
			if (seq.get(0).accept(EQUALS_VISITOR, head)) {
				
				if (seq.getItems().size() == 1) {
					// that would be weird case, but anyway...
					return new EmptyStringConstant(seq.getPosition());
				}
				else if (seq.getItems().size() == 2) {
					return seq.get(1);
				}
				else {
					return new StringSequence(seq.getPosition(), seq.getItems().subList(1, seq.getItems().size()));
				}
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}
	public static IAbstractString removeNullChoice(IAbstractString str) {
		IAbstractString result = flattenStringCollections(str);
		if (result instanceof StringChoice) {
			List<IAbstractString> newOptions = new ArrayList<IAbstractString>();
			for (IAbstractString option : ((StringChoice)result).getItems()) {
				// TODO should distinguish between actual "null" string and null
				if (option instanceof StringConstant && ((StringConstant)option).getConstant().equals("null")) {
					// do nothing
				}
				else {
					newOptions.add(option);
				}
			}
			
			if (newOptions.size() == 1) {
				return newOptions.get(0);
			}
			else { 
				return new StringChoice(str.getPosition(), newOptions);
			}
		}
		else {
			return result;
		}
	}
	
	
}
