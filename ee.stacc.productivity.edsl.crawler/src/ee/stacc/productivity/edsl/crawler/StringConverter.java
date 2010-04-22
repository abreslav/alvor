package ee.stacc.productivity.edsl.crawler;

import java.util.ArrayList;
import java.util.List;

import ee.stacc.productivity.edsl.cache.UnsupportedStringOpEx;
import ee.stacc.productivity.edsl.string.AbstractStringCollection;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringParameter;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;

public class StringConverter {
	
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
				
				if (strSeq.getItems().get(0) instanceof RecursiveStringChoice) {
					RecursiveStringChoice recChoice = (RecursiveStringChoice)strSeq.getItems().get(0);
					if (recChoice.getRecKey() == namedStr.getKey()) {
						
						// return Seq[BaseCase, Repetition(TailOfStrSeq)]
						List<IAbstractString> seqTail = strSeq.getItems().subList(1, strSeq.getItems().size());
						StringRepetition stringRep = new StringRepetition(
								recChoice.getPosition(),
								new StringSequence(strSeq.getPosition(), seqTail));
						
						return new StringSequence(
								str.getPosition(), 
								recChoice.getBase(),
								stringRep);
						// TODO problem if several RecChoices with same key
					}
					else {
						throw new UnsupportedStringOpEx("Unsupported form of NamedString-A");
					}
				}
				else {
					throw new UnsupportedStringOpEx("Unsupported form of NamedString-B");
				}
			}
			else {
				throw new UnsupportedStringOpEx("Unsupported form of NamedString-C");
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
			throw new UnsupportedStringOpEx("Widening inside StringRepetion");
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
}
