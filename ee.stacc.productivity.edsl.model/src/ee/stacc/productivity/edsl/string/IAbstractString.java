package ee.stacc.productivity.edsl.string;


public interface IAbstractString {
	<R, D> R accept(IAbstractStringVisitor<? extends R, ? super D> visitor, D data);
	boolean isEmpty();
	
	IPosition getPosition();
}
