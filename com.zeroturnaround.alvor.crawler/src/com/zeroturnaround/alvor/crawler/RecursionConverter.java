package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.zeroturnaround.alvor.cache.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.string.AbstractStringCollection;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;

public class RecursionConverter {
//	/*
//	 * Assuming that str is recursively referred to in one of it's descendant nodes.
//	 *   
//	 * Lets call recursive reference to str as 'S'. We can think of S as starting nonterminal
//	 * in a grammar, where some right hand sides also contain S.
//	 * 
//	 * (Also assuming, that S is only  kind of recursive call in it, ie. other "local" 
//	 * recursions have been already resolved. TODO this reduces universality of the method) 
//	 * 
//	 * First flatten the structure -- convert str to a choice of flat sequences 
//	 * (ie. without any further choices and without nested sequences).
//	 * The sequences can contain literals, parameters, repetitions ...
//	 * 
//	 * (Actually, those sequences can contain nested choices and sequences if they don't contain S,
//	 * ie. all occurrences of S should be only in top level sequences.
//	 * TODO during creation of the abstract string try to keep recursive calls in top level,
//	 * transform if necessary)
//	 * 
//	 * Then partition resulting sequences into 4 sets: 
//	 * 		(1) S doesn't occur in the sequence
//	 * 		(2) S is first item (and doesn't occur in other positions)
//	 * 		(3) S is last item (and doesn't occur in other positions)
//	 * 		(4) S is in between other items or occurs multiple times (is this always a problem??) 
//	 * 
//	 * Now check resulting sets:
//	 * 		- if there's nothing in set (1) then there is no base case in the recursion,
//	 * 				language is smth like "S -> S" ie. nothing can be generated
//	 * 		- if there's something in (4) then it's not (necessarily?) a regular language
//	 * 		- if there's something in both (2) and (3) then it's not (necessarily?) a regular language
//	 * 
//	 * 		- if there is smth only in (1) and (2):
//	 * 			- start = choice of all things in (1)
//	 * 			- rep   = choice of all things following S in (2)
//	 * 			- result = seq(start, rep)
//	 * 
//	 * 		- if there is smth only in (1) and (3):
//	 * 			- end = choice of all things in (1)
//	 * 			- rep   = choice of all things preceding S in (3)
//	 * 			- result = seq(rep, end)
//	 * 
//	 * 
//	 */
//	public static IAbstractString recursionToRepetition(IAbstractString str) {
//		if (!str.containsRecursion()) {
//			return str;
//		}
//		// TODO recursive string choice and stuff...
//		else {
//			// prepare string
//			IAbstractString prepStr = str;
//			
//			// process children 
//			if (str instanceof AbstractStringCollection) {
//				AbstractStringCollection strColl = (AbstractStringCollection)str;
//				List<IAbstractString> items = new ArrayList<IAbstractString>();
//				for (IAbstractString item : strColl.getItems()) {
//					items.add(recursionToRepetition(item));
//				}
//				if (str instanceof StringChoice) {
//					prepStr = new StringChoice(str.getPosition(), items);
//				}
//				else if (str instanceof StringSequence) {
//					prepStr = new StringSequence(str.getPosition(), items);
//				}
//			}
//			else if (str instanceof StringRepetition) {
//				prepStr = new StringRepetition(str.getPosition(), 
//						recursionToRepetition(((StringRepetition) str).getBody()));
//			}
//			
//			
//			// flatten
////			prepStr = flattenStringCollections(prepStr);
//			
//			if (! (prepStr instanceof StringChoice)) {
//				throw new UnsupportedStringOpEx("Unterminating recursion", str.getPosition());
//			}
//			
////			StringChoice top = (String)
//			
//			return null;
//		}
//		
//	}
	
	/**
	 * str can be a branch of a bigger abstract string, ie some recursive references may have their 
	 * "target" outside 'str'
	 *  
	 * The function transforms all "complete" recursions, ie. when both recursive refs and 
	 * their target node are inside 'str'. All remaining recursive calls are brought up in the structure
	 * so that they are easier to find (caller of this function can locate all remaining recursive 
	 * refs in 2 topmost levels of the result) 
	 * 
	 * 
	 * If str contains recursive calls then return a result StringChoice such that:
	 * 		- it's equivalent to str
	 * 		- all recursive calls are brought as high in the tree as possible
	 *        ie. direct child of result or if child is sequence then direct child of the sequence
	 *      - if any recursive call is pointing to str itself, then recursion is replaced
	 *        by repetition
	 * 
	 * 
	 * 
	 */
	public static List<IAbstractString> removeRecursion(IAbstractString str) {
		
		// simple Abstract Strings
		if (str instanceof StringConstant
				|| str instanceof StringParameter
				|| str instanceof StringCharacterSet
				//|| str instanceof StringRecursiveReference
				) {
			return Arrays.asList(str);			
		}
		else if (str instanceof StringRepetition) {
			// TODO just now i'm assuming that there's no further recursion inside a repetition
			// actually this can happen (i think), but at the moment i just don't know how to deal with it
			if (str.containsRecursion()) {
				throw new UnsupportedStringOpEx("internal problem: Recursion in repetition", str.getPosition());
			}
			else {
				return Arrays.asList(str);			
			}
		}
		
		else {
			assert str instanceof AbstractStringCollection;
			
			// reorganize children into a set of options, so that ... 
			List<IAbstractString> options = new ArrayList<IAbstractString>();
			
			// In case of Option, merge processed children together into new set of options
			if (str instanceof StringChoice) {
				for (IAbstractString item : ((StringChoice)str).getItems()) {
					options.addAll(removeRecursion(item));
				}
			}
			
			// In case of Sequence, "multiply" the choices
			else if (str instanceof StringSequence) {
				/*  Most straightforward solution would be bringing all choices to the top level,
				 *  eg, each resulting option would be a linear sequence or smth simpler.
				 * 
				 *  TODO: this can cause much duplication. I only need that options 
				 *  containing rec-refs are linearized
				 */
				
				for (IAbstractString item : ((StringChoice)str).getItems()) {
					List<IAbstractString> itemOptions = removeRecursion(item);
					assert ! itemOptions.isEmpty();
					
					// options from first piece of the sequence go to the result as they are
					if (options.isEmpty()) { 
						options.addAll(itemOptions);
					}
					// later options should be appended by "multiplying" with existing sequences
					// ie. each result option remains linear, but amount of options is multiplied 
					else { 
						// concatenate each option from 'options' with each option in 'itemOptions'
						// and the result of this becomes new 'options'
						List<IAbstractString> tempOptions = new ArrayList<IAbstractString>();
						
						for (IAbstractString oldOption: options) {
							for (IAbstractString newOption: itemOptions) {
								tempOptions.add(createLinearSequence(oldOption, newOption));
							}
						}
						options = tempOptions;
					}
				}
			}
			return options;
		}
		
		// now the structure is normalized
		
		// TODO remove recursion
		
		// TODO after creating a resulting repetition, check that there's no recursion in it
		// I just don't know how to deal with it
		
		
	}
	
//	/* Restructures str into options of a choice so that all recursive refs 
//	 * are brought up into top-level of it's option (eg. if option is a sequence and it 
//	 * contains a ref-rec, then it should be as direct child of the sequence)
//	 * 
//	 * Options are grouped according to the position of rec-ref
//	 * 
//	 * TODO: If recursion is fully contained in str (ie. target of rec-ref is in str) then 
//	 * recursion is replaced with equivalent repetition or exception is raised
//	 * 
//	 *
//	 */
//	private static StringChoicesByKind bubbleUpRecursion(IAbstractString str) {
//		List<IAbstractString> noRec    = new ArrayList<IAbstractString>();
//		List<IAbstractString> onlyRec  = new ArrayList<IAbstractString>();
//		List<IAbstractString> leftRec  = new ArrayList<IAbstractString>();
//		List<IAbstractString> rightRec = new ArrayList<IAbstractString>();
//		List<IAbstractString> badRec   = new ArrayList<IAbstractString>();
//		
//		if (!str.containsRecursion()) {
//			noRec.add(str);
//		}
////		else if (str instanceof RecRef) {
////			onlyRec.add(str);
////		}
//		else if (str instanceof StringChoice) {
//			for (IAbstractString option : ((StringChoice) str).getItems()) {
//				
//			}
//		}
//		else { // bring recursive references as high as possible
//			assert str instanceof AbstractStringCollection;
//			
//		}
//		
////		return new StringChoicesByKind(noRec, onlyRec, leftRec, rightRec, badRec);
//		ret
//	}
	
	/**
	 * 
	 * @param left should be already linear
	 * @param right should be already linear
	 * @return
	 */
	private static StringSequence createLinearSequence(IAbstractString left, IAbstractString right) {
		List<IAbstractString> items = new ArrayList<IAbstractString>();
		
		if (left instanceof StringSequence) {
			items.addAll(((StringSequence) left).getItems());
		}
		else {
			items.add(left);
		}
		
		if (right instanceof StringSequence) {
			items.addAll(((StringSequence) right).getItems());
		}
		else {
			items.add(right);
		}
		
		return new StringSequence(null, items);
	}

//	/* A helper data structure */
//	private class StringChoicesByKind {
//		public final List<IAbstractString> noRec;
//		public final List<IAbstractString> onlyRec;
//		public final List<IAbstractString> leftRec;
//		public final List<IAbstractString> rightRec;
//		
//		// badRec contains recursive reference in the middle of the sequence 
//		// or contains several recursive references
//		public final List<IAbstractString> badRec;  
//
//		public StringChoicesByKind(
//				List<IAbstractString> noRec,
//				List<IAbstractString> onlyRec,
//				List<IAbstractString> leftRec,
//				List<IAbstractString> rightRec,
//				List<IAbstractString> badRec) {
//			this.noRec = noRec;
//			this.onlyRec = onlyRec;
//			this.leftRec = leftRec;
//			this.rightRec = rightRec;
//			this.badRec = badRec;
//		}
//	}
//	
}
