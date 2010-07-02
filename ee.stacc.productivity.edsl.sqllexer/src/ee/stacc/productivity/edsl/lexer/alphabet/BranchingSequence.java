package ee.stacc.productivity.edsl.lexer.alphabet;

/**
 * Simple implementation of an immutable sequence.
 * 
 * This implementation is based on a "branching" linked list
 * 
 * @author abreslav
 *
 * @param <E> element type
 */
public class BranchingSequence<E> implements ISequence<E> {

	/*
	 * Linked list node, with a reference to a previous item
	 */
	private static final class Item<E> {
		private final Item<E> previous;
		private final E data;
		
		public Item(Item<E> previous, E data) {
			this.previous = previous;
			this.data = data;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			result = prime * result
					+ ((previous == null) ? 0 : previous.hashCode());
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
			@SuppressWarnings("unchecked")
			Item<?> other = (Item) obj;
			if (data == null) {
				if (other.data != null)
					return false;
			} else if (!data.equals(other.data))
				return false;
			if (previous == null) {
				if (other.previous != null)
					return false;
			} else if (!previous.equals(other.previous))
				return false;
			return true;
		}
		
		
	}
	
	private final Item<E> last;
	
	public BranchingSequence() {
		last = null;
	}

	public BranchingSequence(E first) {
		this(null, first);
	}
	
	public BranchingSequence(Item<E> previous, E first) {
		last = new Item<E>(previous, first);
	}
	
	@Override
	public ISequence<E> append(E item) {
		return new BranchingSequence<E>(last, item);
	}

	@Override
	public <R> R fold(R initial, IFoldFunction<R, ? super E> function) {
		return doFold(last, initial, function);
	}

	private <R> R doFold(Item<E> current, R initial, IFoldFunction<R, ? super E> function) {
		if (current == null) {
			return initial;
		}
		R newInitial = doFold(current.previous, initial, function);
		return function.body(newInitial, current.data, current == last);
	}
	
	@Override
	public boolean isEmpty() {
		return last == null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((last == null) ? 0 : last.hashCode());
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
		@SuppressWarnings("unchecked")
		BranchingSequence<?> other = (BranchingSequence) obj;
		if (last == null) {
			if (other.last != null)
				return false;
		} else if (!last.equals(other.last))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "<" + fold(new StringBuilder(), new IFoldFunction<StringBuilder, E>() {
			
			@Override
			public StringBuilder body(StringBuilder result, E arg, boolean last) {
				return result.append(arg).append(last ? "" : " ");
			}
			
		}) + ">";
	}
	
	
}
