package com.zeroturnaround.alvor.string.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.zeroturnaround.alvor.string.AbstractStringCollection;
import com.zeroturnaround.alvor.string.AbstractStringEqualsVisitor;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRecursion;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;

/**
 * @author abreslav
 *
 */
public class AbstractStringOptimizer {

	private static IAbstractStringVisitor<Boolean, IAbstractString> EQUALS_VISITOR = new AbstractStringEqualsVisitor(false);
	
	public static boolean abstractStringEquals(IAbstractString a, IAbstractString b) {
		return a.accept(EQUALS_VISITOR, b);
	}
	
	private static IAbstractStringVisitor<IAbstractString, Void> OPTIMIZE_VISITOR = new IAbstractStringVisitor<IAbstractString, Void>() {
		
		@Override
		public IAbstractString visitStringCharacterSet(
				StringCharacterSet characterSet, Void data) {
			return characterSet;
		}
		
		@Override
		public IAbstractString visitStringConstant(
				StringConstant stringConstant, Void data) {
			return stringConstant;
		}
		
		@Override
		public IAbstractString visitStringParameter(
				StringParameter stringParameter, Void data) {
			return stringParameter;
		}
		
		@Override
		public IAbstractString visitStringRepetition(
				StringRepetition stringRepetition, Void data) {
			IAbstractString body = stringRepetition.getBody();
			IAbstractString optimized = optimize(body);
			if (optimized == body) {
				return stringRepetition;
			} else {
				return new StringRepetition(null, optimized);
			}
		}
		
		@Override
		public IAbstractString visitStringSequence(
				StringSequence stringSequence, Void data) {
			List<IAbstractString> optimized = getOptimizedContents(stringSequence);
			if (optimized == null) {
				optimized = stringSequence.getItems();
			}
			List<IAbstractString> result = new ArrayList<IAbstractString>();
			for (IAbstractString s : optimized) {
				if (s instanceof StringSequence) {
					StringSequence seq = (StringSequence) s;
					result.addAll(seq.getItems());
				} else {
					result.add(s);
				}
			}
			return listToAS(result);
		}
		
		@Override
		public IAbstractString visitStringChoice(StringChoice stringChoice,
				Void data) {
			stringChoice = simpleOptimizeChoice(stringChoice);
			List<IAbstractString> items = stringChoice.getItems();
			if (items.size() == 2) {
				IAbstractString a = items.get(0);
				IAbstractString b = items.get(1);
				IAbstractString opt = optimizeChoice(a, b);
				if (opt != null) {
					return opt;
				}
			}
			return stringChoice;
		}

		@Override
		public IAbstractString visitStringRecursion(
				StringRecursion stringRecursion, Void data) {
			return stringRecursion;
		}
	};

	public static IAbstractString optimize(IAbstractString string) {
		return string.accept(OPTIMIZE_VISITOR, null);
	}
	
	private static StringChoice simpleOptimizeChoice(
			StringChoice stringChoice) {
		List<IAbstractString> optimized = getOptimizedContents(stringChoice);
		if (optimized == null) {
			return stringChoice;
		}
		return new StringChoice(null, optimized);
	}
	
	private static IAbstractString optimizeChoice(IAbstractString a,
			IAbstractString b) {
		if (a instanceof StringSequence) {
			StringSequence sa = (StringSequence) a;
			if (b instanceof StringSequence) {
				StringSequence sb = (StringSequence) b;
				Iterator<IAbstractString> saItems = sa.getItems().iterator();
				Iterator<IAbstractString> sbItems = sb.getItems().iterator();
				
				List<IAbstractString> common = new ArrayList<IAbstractString>();
				
				while (saItems.hasNext() && sbItems.hasNext()) {
					IAbstractString ai = saItems.next();
					IAbstractString bi = sbItems.next();
					
					if (!abstractStringEquals(ai, bi)) {
						break;
					}					
					common.add(ai);
				}
				
				if (!common.isEmpty()) {

					List<IAbstractString> restA = sa.getItems().subList(common.size(), sa.getItems().size());
					List<IAbstractString> restB = sb.getItems().subList(common.size(), sb.getItems().size());
					return new StringSequence(
								(IPosition) null, 
								listToAS(common), 
								new StringChoice(
										(IPosition) null, 
										listToAS(restA), 
										listToAS(restB))
							);
						
				}
			} else {
				List<IAbstractString> saItems = sa.getItems();
				if (!saItems.isEmpty() && abstractStringEquals(saItems.get(0), b)) {
					IAbstractString newSA = new StringSequence(saItems.subList(1, saItems.size()));
					return new StringSequence((IPosition) null, b, new StringChoice(newSA, new StringConstant("")));
				}
			}
		} else {
			if (b instanceof StringSequence) {
				return optimizeChoice(b, a);
			}
		}
		return null;
	}

	private static IAbstractString listToAS(List<IAbstractString> common) {
		IAbstractString prefix;
		if (common.size() == 1) {
			prefix = common.get(0);
		} else {
			prefix = new StringSequence(null, common);
		}
		return prefix;
	}

	private static List<IAbstractString> getOptimizedContents(
			AbstractStringCollection colleciton) {
		List<IAbstractString> optimized = null;
		int i = 0;
		List<IAbstractString> items = colleciton.getItems();
		for (IAbstractString as : items) {
			IAbstractString optimizedAS = optimize(as);
			if (optimized != null || optimizedAS != as) {
				if (optimized == null) {
					optimized = new ArrayList<IAbstractString>();
					optimized.addAll(items.subList(0, i));
				} 
				optimized.add(optimizedAS);
			}
			i++;
		}
		return optimized;
	}


}
