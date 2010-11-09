package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.List;

import com.zeroturnaround.alvor.cache.PositionUtil;
import com.zeroturnaround.alvor.cache.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.string.AbstractStringCollection;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;

public class StringConverter {
	private static IAbstractStringVisitor<Boolean, IAbstractString> EQUALS_VISITOR_EX = new AbstractStringEqualsVisitorExtended();
	
	public static IAbstractString widenToRegular(IAbstractString str) {
		return widenFlatToRegular(flattenStringCollections(str));
	}
	
	private static IAbstractString widenFlatToRegular(IAbstractString str) {
		if (str instanceof NamedString) {
			NamedString namedStr = (NamedString)str;
			if (!hasRecursiveChoice(str, namedStr.getKey())) {
				return widenFlatToRegular(namedStr.getBody());
			}
			else if (namedStr.getBody() instanceof StringChoice) {
				return widenFlatToRegular(namedChoiceToChoiceOfNamed(namedStr));
			}
			else if (namedStr.getBody() instanceof StringSequence) {
				StringSequence strSeq = (StringSequence)namedStr.getBody();
				
				if (strSeq.get(0) instanceof RecursiveStringChoice) {
					RecursiveStringChoice recChoice = (RecursiveStringChoice)strSeq.get(0);
					if (recChoice.getRecKey() == namedStr.getKey()) {
						
						// return Seq[BaseCase, Repetition(TailOfStrSeq)]
						List<IAbstractString> seqTail = strSeq.getItems().subList(1, strSeq.getItems().size());
						StringRepetition stringRep = new StringRepetition(
								recChoice.getPosition(),
								new StringSequence(strSeq.getPosition(), seqTail));
						
						// TODO problem getting unique positions, using hack for now
						return new StringSequence(
								PositionUtil.shiftPosition(namedStr.getPosition(),1,0), 
								recChoice.getBase(),
								stringRep);
						// TODO problem if several RecChoices with same key
					}
					else {
						throw new UnsupportedStringOpEx("Unsupported form of NamedString-A", str.getPosition());
					}
				}
				else {
					throw new UnsupportedStringOpEx("Unsupported form of NamedString-B", str.getPosition());
				}
			}
			else {
				throw new UnsupportedStringOpEx("Unsupported form of NamedString-C", str.getPosition());
			}
		}
		else if (str instanceof StringParameter) {
			return str;
		} 
		else if (str instanceof StringConstant) {
			return str;
		} 
		else if (str instanceof StringCharacterSet) {
			return str;
		} 
		else if (str instanceof StringRepetition) {
			throw new UnsupportedStringOpEx("Widening inside StringRepetion", str.getPosition());
		}
		else if (str instanceof StringChoice) {
			List<IAbstractString> resultItems = new ArrayList<IAbstractString>();
			for (IAbstractString item : ((StringChoice)str).getItems()) {
				resultItems.add(widenFlatToRegular(item));
			}
			return new StringChoice(str.getPosition(), resultItems);
		}
		else if (str instanceof StringSequence) {
			List<IAbstractString> resultItems = new ArrayList<IAbstractString>();
			for (IAbstractString item : ((StringSequence)str).getItems()) {
				resultItems.add(widenFlatToRegular(item));
			}
			return new StringSequence(str.getPosition(), resultItems);
		}
		else if (str instanceof RecursiveStringChoice) {
			// TODO
			return str;
		}
		else {
			throw new IllegalArgumentException("widenFlatToRegular: " + str.getClass());
		}
	}
	
	private static StringChoice namedChoiceToChoiceOfNamed(NamedString str) {
		// FIXME this actually alters semantics!
		
		// create choice of NamedString instead
		List<IAbstractString> options = new ArrayList<IAbstractString>();
		for (IAbstractString opt : ((StringChoice)str.getBody()).getItems()) {
			options.add(new NamedString(str.getPosition(), str.getKey(), opt));
		}
		return new StringChoice(str.getPosition(), options);
	}
	
	private static boolean hasRecursiveChoice(IAbstractString str, Object key) {
		if (str instanceof RecursiveStringChoice) {
			RecursiveStringChoice recChoice = (RecursiveStringChoice)str;
			if (recChoice.getRecKey() == key) {
				return true;
			}
			else {
				return hasRecursiveChoice(recChoice.getBase(), key);
			}
		}
		else if (str instanceof StringConstant) {
			return false;
		}
		else if (str instanceof StringCharacterSet) {
			return false;
		}
		else if (str instanceof StringParameter) {
			return false;
		}
		else if (str instanceof NamedString) {
			return hasRecursiveChoice(((NamedString)str).getBody(), key);
		}
		else if (str instanceof StringRepetition) {
			return hasRecursiveChoice(((StringRepetition)str).getBody(), key);
		}
		else if (str instanceof AbstractStringCollection) {
			for (IAbstractString as : ((AbstractStringCollection)str).getItems()) {
				if (hasRecursiveChoice(as, key)) {
					return true;
				}
			}
			return false;
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	
	public static boolean includesStringExtensions(IAbstractString str) {
		if (str instanceof RecursiveStringChoice) {
			return true;
		}
		else if (str instanceof StringConstant) {
			return false;
		}
		else if (str instanceof StringCharacterSet) {
			return false;
		}
		else if (str instanceof StringParameter) {
			return false;
		}
		else if (str instanceof NamedString) {
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
		
		else if (str instanceof NamedString) {
			NamedString named = (NamedString)str;
			return new NamedString(named.getPosition(), named.getKey(), 
					flattenStringCollections(((NamedString)str).getBody()));
		}
		else if (str instanceof StringRepetition) {
			StringRepetition rep = (StringRepetition)str;
			return new StringRepetition(rep.getPosition(),  
					flattenStringCollections(((StringRepetition)str).getBody()));
		}
		else if (str instanceof RecursiveStringChoice) {
			RecursiveStringChoice recChoice = (RecursiveStringChoice)str;
			return new RecursiveStringChoice(recChoice.getPosition(),  
					flattenStringCollections(((RecursiveStringChoice)str).getBase()),
					recChoice.getRecKey());
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
								new StringConstant(PositionUtil.getDummyPosition(), "", "\"\"")));
			}
			else {
				return str;
			}
		}
		else {
			return str;
		}
		/*
		 * TODO following needs some checking
		else if (str.get(0) instanceof StringSequence) {
			StringSequence seq0 = (StringSequence)str.get(0);
			if (seq0.getItems().isEmpty()) {
				return str;
			}
			
			IAbstractString head0 = seq0.get(0);
			List<IAbstractString> tails = new ArrayList<IAbstractString>();
			
			for (IAbstractString item : str.getItems()) {
				if (item instanceof StringSequence) {
					StringSequence seqN = (StringSequence)item;
					if (seqN.getItems().isEmpty()) {
						return str;
					}
					IAbstractString headN = seqN.get(0);
					if (assumeSharedStrings && headN == head0
						// FIXME: following is not quite correct 
						|| !assumeSharedStrings && headN.toString().equals(str.toString())) {
						if (seqN.getItems().size() == 1) {
							tails.add(new StringConstant(seqN.getPosition(), "", "\"\""));
						}
						else if (seqN.getItems().size() == 2) {
							tails.add(seqN.get(1));
						}
						else if (seqN.getItems().size() > 2) {
							tails.add(new StringSequence
									(seqN.getItems().subList(1, seqN.getItems().size())));
						}
					}
				}
				else {
					// found nonsuitable item
					return str;
				}
			}
		}
		*/
	}
	
	private static IAbstractString getTailIfHasHead(IAbstractString str, 
			IAbstractString head) {
		if (str instanceof StringSequence) {
			StringSequence seq = (StringSequence)str;
			if (seq.getItems().isEmpty()) {
				return null;
			}
			
			if (seq.get(0).accept(EQUALS_VISITOR_EX, head)) {
				
				if (seq.getItems().size() == 1) {
					// that would be weird case, but anyway...
					return new StringConstant(seq.getPosition(), "", "\"\"");
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
	
	/*
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
	 * 		(3) S is last item (and doesn't in other positions)
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
	public static IAbstractString recursionToRepetition(IAbstractString str) {
		return null;
	}
}
