package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.VariableDeclaration;

public class NameAssignment extends NameUsage {
	private Assignment.Operator operator;
	private Expression leftHandSide;
	private Expression rightHandSide;
	private ASTNode node;
	
	public NameAssignment(Assignment assignment) {
		this.node = assignment;
		this.operator = assignment.getOperator();
		this.leftHandSide = assignment.getLeftHandSide();
		this.rightHandSide = assignment.getRightHandSide();
	}
	
	public NameAssignment(VariableDeclaration decl) {
		this.node = decl;
		this.operator = Assignment.Operator.ASSIGN;
		this.rightHandSide = decl.getInitializer();
		this.leftHandSide = decl.getName();
	}
	
	public ASTNode getNode() {
		return node;
	}
	
	public Name getName() {
		return (Name)leftHandSide; 
	}
	
	public Expression getRightHandSide() {
		return rightHandSide;
	}
	
	public Assignment.Operator getOperator() {
		return operator;
	}
}
