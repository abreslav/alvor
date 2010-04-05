package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Name;

public class NameAssignment extends NameUsage {
	private Assignment.Operator operator;
	private Expression rightHandSide;
	private Expression leftHandSide;
	
	public NameAssignment(Expression leftHandSide, Assignment.Operator operator, Expression expr) {
		this.operator = operator;
		this.leftHandSide = leftHandSide;
		this.rightHandSide = expr;
	}
	
	public Expression getRightHandSide() {
		return rightHandSide;
	}
	
	public Expression getLeftHandSide() {
		return leftHandSide;
	}
	
	public Name getName() {
		return (Name)leftHandSide; 
	}
	
	public Assignment.Operator getOperator() {
		return operator;
	}
}
