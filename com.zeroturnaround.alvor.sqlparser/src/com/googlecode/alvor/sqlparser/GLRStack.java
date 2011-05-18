package com.googlecode.alvor.sqlparser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A "multi-stack" for GLR-like parsing. It contains many stacks and drops those that come to an erro state.
 * No space optimization is performed, so it is not really GLR parsing (it might get worse than N^3), but the
 * principle is the same.
 * 
 * @author abreslav
 *
 */
public class GLRStack implements IParserStackLike {

	/**
	 * A factory that creates GLR stacks built of bounded stacks
	 */
	public static final IStackFactory<GLRStack> FACTORY = new IStackFactory<GLRStack>() {
		
		@Override
		public GLRStack newStack(IParserState state) {
			GLRStack glrStack = new GLRStack();
			glrStack.addVariant(BoundedStack.getFactory(100, new ErrorState(null, null)).newStack(state));
			return glrStack;
		}
	};
	
	private final Set<IParserStack> variants = new HashSet<IParserStack>();
	private final Collection<IParserStack> variantsRO = Collections.unmodifiableCollection(variants);
	private IParserState errorOnTop = null;
	
	/**
	 * @return a collection of simple stacks this multi-stack is comprised of
	 */
	public Collection<IParserStack> getVariants() {
		return variantsRO;
	}

	/**
	 * Adds a new single-stack to this multi-stack
	 */
	public void addVariant(IParserStack stack) {
		if (!stack.hasErrorOnTop()) {
			variants.add(stack);
			errorOnTop = null;
		} else {
			if (variants.isEmpty() && errorOnTop == null) {
				errorOnTop = stack.getErrorOnTop();
			}
		}
	}

	@Override
	public IParserState getErrorOnTop() {
		if (errorOnTop == null && variants.isEmpty()) {
			throw new IllegalStateException();
		}
		return errorOnTop;
	}

	@Override
	public boolean topAccepts() {
		for (IParserStack stack : variants) {
			if (!stack.topAccepts()) {
				return false;
			}
		}
		return !variants.isEmpty();
	}

	@Override
	public boolean hasErrorOnTop() {
		return getErrorOnTop() != null;
	}

	@Override
	public String toString() {
		return variants.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((errorOnTop == null) ? 0 : errorOnTop.hashCode());
		result = prime * result
				+ ((variants == null) ? 0 : variants.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GLRStack other = (GLRStack) obj;
		if (errorOnTop == null) {
			if (other.errorOnTop != null)
				return false;
		} else if (!errorOnTop.equals(other.errorOnTop))
			return false;
		if (variants == null) {
			if (other.variants != null)
				return false;
		} else if (!variants.equals(other.variants))
			return false;
		return true;
	}

}
