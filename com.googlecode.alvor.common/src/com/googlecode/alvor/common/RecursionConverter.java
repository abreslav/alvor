package com.googlecode.alvor.common;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.alvor.common.EmptyStringConstant;
import com.googlecode.alvor.string.AbstractStringCollection;
import com.googlecode.alvor.string.DummyPosition;
import com.googlecode.alvor.string.IAbstractString;
import com.googlecode.alvor.string.IPosition;
import com.googlecode.alvor.string.StringCharacterSet;
import com.googlecode.alvor.string.StringChoice;
import com.googlecode.alvor.string.StringConstant;
import com.googlecode.alvor.string.StringParameter;
import com.googlecode.alvor.string.StringRecursion;
import com.googlecode.alvor.string.StringRepetition;
import com.googlecode.alvor.string.StringSequence;

/*
 * OVERALL IDEA OF THE ALGORITHM
 * TODO review this
 * 
 * Assuming that str is recursively referred to in one of it's descendant nodes.
 *   
 * Lets call recursive reference to str as 'S'. We can think of S as starting nonterminal
 * in a grammar, where some right hand sides also contain S.
 * 
 * (Also assuming, that S is only  kind of recursive call in it, ie. other "local" 
 * recursions have been already resolved. TODO this reduces universality of the method) 
 * 
 * First flatten the structure -- convert str to a choice of flat sequences 
 * (ie. without any further choices and without nested sequences).
 * The sequences can contain literals, parameters, repetitions ...
 * 
 * (Actually, those sequences can contain nested choices and sequences if they don't contain S,
 * ie. all occurrences of S should be only in top level sequences.
 * TODO during creation of the abstract string try to keep recursive calls in top level,
 * transform if necessary)
 * 
 * Then partition resulting sequences into 4 sets: 
 * 		(1) S doesn't occur in the sequence
 * 		(2) S is first item (and doesn't occur in other positions)
 * 		(3) S is last item (and doesn't occur in other positions)
 * 		(4) S is in between other items or occurs multiple times (is this always a problem??) 
 * 
 * Now check resulting sets:
 * 		- if there's nothing in set (1) then there is no base case in the recursion,
 * 				language is smth like "S -> S" ie. nothing can be generated
 * 		- if there's something in (4) then it's not (necessarily?) a regular language
 * 		- if there's something in both (2) and (3) then it's not (necessarily?) a regular language
 * 
 * 		- if there is smth only in (1) and (2):
 * 			- start = choice of all things in (1)
 * 			- rep   = choice of all things following S in (2)
 * 			- result = seq(start, rep)
 * 
 * 		- if there is smth only in (1) and (3):
 * 			- end = choice of all things in (1)
 * 			- rep   = choice of all things preceding S in (3)
 * 			- result = seq(rep, end)
 * 
 * 
 */
public class RecursionConverter {
	
//	public static IAbstractString checkRecursionToRepetition(IAbstractString str) {
//		if (str.containsRecursion()) {
//			throw new UnsupportedStringOpEx("Unsupported modification scheme in loop", str.getPosition());
//			
//			// TODO put back when path-sensitivity is done
////			IAbstractString result = recursionToRepetition(str);
////			if (result instanceof StringChoice) {
////				result = StringConverter.optimizeChoice((StringChoice) result);
////			}
////			return StringConverter.flattenStringCollections(result);
//		}
//		else {
//			return str;
//		}
//	}
	
	/**
	 * It's assumed, that abstract string is constructed so that all refursive references are descendants
	 * of their target node. Note that this str can be a branch of a bigger abstract string, 
	 * ie some recursive references may have their "target" upwards of 'str' 
	 *  
	 * The function transforms all "complete" recursions, ie. cases where both recursive refs and 
	 * their target node are inside 'str'. All remaining recursive calls are brought up in the structure,
	 * so that they are easier to find (caller of this function can locate all remaining recursive 
	 * refs in 2 topmost levels of the result). Result is a list of options (possibly
	 * with one item) and each option is either something "simple" (constant, rec-ref) 
	 * or a "linear sequence" (ie. there are no choices or nested sequences in it).
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
	public static IAbstractString recursionToRepetition(IAbstractString str) {

		// FIXME remove recursion from function-ref arguments independently
		
		
		// simple Abstract Strings
		if (str instanceof StringConstant
				|| str instanceof StringParameter
				|| str instanceof StringCharacterSet
				|| str instanceof StringRecursion // this will be handled somewhere else in call stack 
				) {
			return str;			
		}
		else if (str instanceof StringRepetition) {
			// TODO just now i'm assuming that there's no further recursion inside a repetition
			// actually this can happen (i think), but at the moment i just don't know how to deal with it
			if (str.containsRecursion()) {
				throw new UnsupportedStringOpEx("internal problem: Recursion in repetition", str.getPosition());
			}
			else {
				return str;			
			}
		}
		
		else if (str instanceof AbstractStringCollection) {
			// reorganize children into a set of options, so that each option is either 
			// "simple" node or "linear sequence"
			List<IAbstractString> options = new ArrayList<IAbstractString>();
			
			// In case of Option, merge processed children together into new set of options
			if (str instanceof StringChoice) {
				for (IAbstractString item : ((StringChoice)str).getItems()) {
					IAbstractString processedChild = recursionToRepetition(item);
					if (processedChild instanceof StringChoice) {
						// "linearize" nested options
						options.addAll(((StringChoice) processedChild).getItems());
					}
					else {
						options.add(processedChild);						
					}
				}
			}
			
			// In case of Sequence, "multiply" the choices
			else if (str instanceof StringSequence) {
				// TODO: currently this way of "normalizing" creates lots of duplication in the result string
				// Should treat branches without rec-refs differently (those may contain inner choices)
				
				for (IAbstractString item : ((StringSequence)str).getItems()) {
					IAbstractString processedChild = recursionToRepetition(item);
					
					// processed version of first piece of the sequence go to the result as they are
					if (options.isEmpty()) {
						if (processedChild instanceof StringChoice) {
							// linearize nested options
							options.addAll(((StringChoice) processedChild).getItems());
						}
						else {
							options.add(processedChild);
						}
					}
					// later items need more care:
					//   - simple items or sequences should be appended to all options
					//   - choices should be "multiplied" with existing choices
					// Each item in 'options' remains linear, but number of options may increase 
					else { 
						// concatenate each option from 'options' with each option in 'itemOptions'
						// and the result of this becomes new 'options'
						List<IAbstractString> tempOptions = new ArrayList<IAbstractString>();
						
						for (IAbstractString oldOption: options) {
							if (processedChild instanceof StringChoice) {
								for (IAbstractString newOption: ((StringChoice) processedChild).getItems()) {
									tempOptions.add(createLinearSequence(oldOption, newOption));
								}
							}
							else {
								tempOptions.add(createLinearSequence(oldOption, processedChild));
							}
						}
						options = tempOptions;
					}
				}
			}
			
			//System.out.println("intermediate: " + new StringChoice(str.getPosition(), options));
			
			return preparedRecursionToRepetition(str.getPosition(), options);
		}
		
		else {
			throw new IllegalArgumentException("Unknown abstract string: " + str.getClass());
		}
		
		// FIXME remove recursion from function-ref arguments independently
		
		// TODO after creating a resulting repetition, check that there's no recursion in it
	}
	
	/**
	 *  'pos' is position of the string being converted (here pos is used as primary key).
	 * 
	 *  The structure of the string is given as set of linear options. An item in 'options'
	 *  can be either a "simple" node (constant, recursion, ...) or "linear sequence" (ie. sequence 
	 *  of simple nodes).
	 *  
	 *  Somewhere in 'options' can be recursive reference to 'pos': 
	 *    - If it doesn't occur, then it means this node is not target for some recursive references
	 *    
	 *    - if rec-ref to pos occurs only in the beginnings of sequences (or alone) then
	 *      the grammar is left-regular (left-linear). 
	 *      The choice of tails (T) of those sequences becomes a repetition and result is seq of 
	 *      remaining nonrecursive choices (N) and T* ie. (N T*)
	 *      Final result needs to use + instead of *, so result will be (N | (N T+))
	 *       
	 *    - analoguous if rec-ref occurs only in ends of the sequences (or alone)
	 *     
	 *    - if position of rec-ref varies (or there is several different recursive references)
	 *      then throw exception
	 *      TODO maybe I can handle different recursive references???
	 *  
	 */
	private static IAbstractString preparedRecursionToRepetition(IPosition pos, List<IAbstractString> options) {
		List<IAbstractString> suffixes = new ArrayList<IAbstractString>(); // tails of sequences where pos reference is in first position
		List<IAbstractString> prefixes = new ArrayList<IAbstractString>(); // beginnings of sequences where pos reference is in last position
		List<IAbstractString> nonrec = new ArrayList<IAbstractString>(); // options not containing rec ref to pos
		boolean hasOnlyRecOptions = false; // set if some option is just recursive reference to pos
		
		for (IAbstractString option : options) {
			if (!option.containsRecursion()) {
				nonrec.add(option);
			}
			else if (option instanceof StringRecursion) { 
				if (((StringRecursion) option).getPosition().equals(pos)) {
					hasOnlyRecOptions = true;
				} 
			}
			else if (option instanceof StringSequence) {
				StringSequence seq = (StringSequence) option;
				boolean optionHasThisRec = false;
				boolean optionHasOtherRec = false;
				
				for (int i = 0; i < seq.getItems().size(); i++) {
					if (seq.get(i) instanceof StringRecursion) {
						StringRecursion rec = (StringRecursion)seq.get(i);
						if (!rec.getPosition().equals(pos)) {
							optionHasOtherRec = true;
						} 
						else {
							optionHasThisRec = true;
							int size = seq.getItems().size();
							if (i == 0) {
								suffixes.add(partOfSequence(seq, 1, size));
							}
							else if (i == size-1) {
								prefixes.add(partOfSequence(seq, 0, size-1));
							}
							else {
								throw new UnsupportedStringOpEx("Unsupported recursion in abstract string (middle position)", pos);
							}
						}
						
					}
				}
				
				if (optionHasThisRec && optionHasOtherRec) {
					// TODO maybe it's not so bad
					throw new UnsupportedStringOpEx("Unsupported (mutual) recursion in abstract string", pos);
				}
				else if (!optionHasThisRec) {
					nonrec.add(option);
				}
			}
			else {
				throw new IllegalArgumentException("Wrong abstract string in prepared option: " + option.getClass()
						+ "\noption=" + option + "\n\nall options=" + options);
			}
			
		}
		
		
		// now analyze the results
		if (suffixes.isEmpty() && prefixes.isEmpty() && !hasOnlyRecOptions) {
			// no recursive calls to pos, just return (as choice)
			// assert options.equals(nonrec); 
			return createChoiceIfNecessary(pos, options);
		}
		else if (hasOnlyRecOptions && suffixes.isEmpty() && prefixes.isEmpty() && nonrec.isEmpty()) {
			throw new UnsupportedStringOpEx("Unsupported recursion in abstract string (diverging)", pos);
		}
		else if (!suffixes.isEmpty() && !prefixes.isEmpty()) {
			throw new UnsupportedStringOpEx("Unsupported recursion in abstract string (mixed position)", pos);
		}
		else { // great success! can convert recursion to repetition!
			assert !suffixes.isEmpty() || !prefixes.isEmpty();
			
			List<IAbstractString> repItems = null;
			if  (!suffixes.isEmpty()) {
				repItems = suffixes;
			} else {
				repItems = prefixes;
			}
			
			StringRepetition rep = new StringRepetition(new DummyPosition(), 
					createChoiceIfNecessary(new DummyPosition(), repItems));
			
			if (nonrec.isEmpty()) {
				// ie. no nonrepetive starting or ending parts
				return new StringChoice(new DummyPosition(), 
						new EmptyStringConstant(), rep);
			}
			else {
				IAbstractString nonrep = createChoiceIfNecessary(new DummyPosition(), nonrec);
				
				StringSequence starVersion = null;
				if (!suffixes.isEmpty()) {
					starVersion = new StringSequence(new DummyPosition(), nonrep, rep);
				} 
				else {
					starVersion = new StringSequence(new DummyPosition(), rep, nonrep);
				}
				
				// starVersion would be fine, if StringRepetition meant 0 or more
				// now need to include choice that doesn't include any repetition
				return new StringChoice(new DummyPosition(), nonrep, starVersion);
				
			}
		}
	}
	
	/**
	 * 
	 * @param left is assumed to be already linear
	 * @param right is assumed to be already linear
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
		
		return new StringSequence(new DummyPosition(), items);
	}
	
	private static IAbstractString partOfSequence(StringSequence seq, int fromIndex, int toIndex) {
		if (fromIndex - toIndex == 1) {
			return seq.get(fromIndex);
		}
		else {
			return new StringSequence(seq.getPosition(), seq.getItems().subList(fromIndex, toIndex));
		}
	}
	
	private static IAbstractString createChoiceIfNecessary(IPosition pos, List<IAbstractString> options) {
		if (options.size() == 1) {
			return options.get(0);
		}
		else {
			return new StringChoice(pos, options);
		}
	}
}
