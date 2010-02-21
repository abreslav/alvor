package ee.stacc.productivity.edsl.lexer.alphabet;

public interface ISequence<E> {
	
	public interface IFoldFunction<R, A> {
		R body(R init, A arg, boolean last);
	}
	
	<R> R fold(R initial, IFoldFunction<R, ? super E> function);
	boolean isEmpty();
	
	ISequence<E> append(E item);
}
