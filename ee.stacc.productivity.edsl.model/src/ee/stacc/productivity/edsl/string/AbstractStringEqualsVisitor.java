/**
 * 
 */
package ee.stacc.productivity.edsl.string;

import java.util.Iterator;
import java.util.List;

public class AbstractStringEqualsVisitor implements
		IAbstractStringVisitor<Boolean, IAbstractString> {
	@Override
	public Boolean visitStringCharacterSet(
			StringCharacterSet me, IAbstractString data) {
		if (data instanceof StringCharacterSet) {
			StringCharacterSet other = (StringCharacterSet) data;
			return areEqual(me.getContents(), other.getContents())
			&& positionEquals(me, other);
		}
		return false;
	}

	@Override
	public Boolean visitStringChoice(StringChoice me,
			IAbstractString data) {
		if (data instanceof StringChoice) {
			StringChoice other = (StringChoice) data;
			return positionEquals(me, other)
				&& contentEquals(me.getItems(), other.getItems());
		}
		return false;
	}

	@Override
	public Boolean visitStringConstant(StringConstant me,
			IAbstractString data) {
		if (data instanceof StringConstant) {
			StringConstant other = (StringConstant) data;
			return positionEquals(me, other)
				&& areEqual(me.getEscapedValue(), other.getEscapedValue());
		}
		return false;
	}

	@Override
	public Boolean visitStringParameter(
			StringParameter me, IAbstractString data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Boolean visitStringRepetition(
			StringRepetition me, IAbstractString data) {
		if (data instanceof StringRepetition) {
			StringRepetition other = (StringRepetition) data;
			return positionEquals(me, other)
				&& abstractStringEquals(me.getBody(), other.getBody());
		}
		return false;
	}

	private boolean abstractStringEquals(IAbstractString a, IAbstractString b) {
		return a.accept(this, b);
	}

	@Override
	public Boolean visitStringSequence(StringSequence me,
			IAbstractString data) {
		if (data instanceof StringSequence) {
			StringSequence other = (StringSequence) data;
			return positionEquals(me, other)
				&& contentEquals(me.getItems(), other.getItems());
		}
		return false;
	}

	private boolean contentEquals(List<IAbstractString> a,
			List<IAbstractString> b) {
		if (a.size() != b.size()) {
			return false;
		}
		Iterator<IAbstractString> ai = a.iterator();
		Iterator<IAbstractString> bi = b.iterator();
		for (;ai.hasNext();) {
			IAbstractString as = ai.next();
			IAbstractString bs = bi.next();
			if (!abstractStringEquals(as, bs)) {
				return false;
			}
		}
		return true;
	}

	private static <T> boolean areEqual(T a, T b) {
		if (a == b) {
			return true;
		}
		if (a != null) {
			return a.equals(b);
		}
		return false;
	}
	
	private static boolean positionEquals(IAbstractString a, IAbstractString b) {
		return areEqual(a.getPosition(), b.getPosition());
	}

}