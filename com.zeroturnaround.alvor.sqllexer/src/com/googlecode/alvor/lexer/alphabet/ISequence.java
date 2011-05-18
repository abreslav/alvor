package com.googlecode.alvor.lexer.alphabet;

/**
 * Represents an immutable sequence of objects.
 * 
 * @author abreslav
 *
 * @param <E> type of objects in this sequence
 */
public interface ISequence<E> {
	
	/**
	 * A fold function.
	 * 
	 * Used as a strategy to process sequences.
	 *  
	 * @author abreslav
	 *
	 * @param <R> return type 
	 * @param <A> argument type (sequence element)
	 */
	public interface IFoldFunction<R, A> {
		
		/**
		 * This method is executed for each element in the sequence
		 * @param result the result value returned by the call to this method on the previous item (or initial result value) 
		 * @param arg the current item of the sequence
		 * @param last true iff the current item is the last one in the sequence
		 * @return the result of processing the sequence up to the current item
		 */
		R body(R result, A arg, boolean last);
	}
	
	/**
	 * Folds the sequence with the given strategy.
	 * @param <R> return type
	 * @param initial initial value (what to return on an empty sequence)
	 * @param function fold strategy
	 * @return result of a chain of calls to {@param function}
	 */
	<R> R fold(R initial, IFoldFunction<R, ? super E> function);
	
	/**
	 * @return true iff the sequence is empty
	 */
	boolean isEmpty();
	
	/**
	 * Creates a new sequence by appending an item to the current one
	 * @param item element to append
	 * @return a new sequence (one item longer)
	 */
	ISequence<E> append(E item);
}
