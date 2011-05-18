package com.googlecode.alvor.tracker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclaration;

public class NameAssignment extends NameUsage {
	private Assignment.Operator operator;
	private Expression leftHandSide;
	private Expression rightHandSide;
	private ASTNode assOrDecl;
	
	public NameAssignment(Assignment assignment) {
		this.assOrDecl = assignment;
		this.operator = assignment.getOperator();
		this.leftHandSide = assignment.getLeftHandSide();
		this.rightHandSide = assignment.getRightHandSide();
	}
	
	public NameAssignment(VariableDeclaration decl) {
		this.assOrDecl = decl;
		this.operator = Assignment.Operator.ASSIGN;
		this.rightHandSide = decl.getInitializer();
		this.leftHandSide = decl.getName();
	}
	
	public ASTNode getAssignmentOrDeclaration() {
		return assOrDecl;
	}
	
	@Override
	public ASTNode getMainNode() {
		return assOrDecl;
	}
	
	public Expression getLeftHandSide() {
		return leftHandSide;
	}
	
	public Expression getRightHandSide() {
		return rightHandSide;
	}
	
	public Assignment.Operator getOperator() {
		return operator;
	}
	
}
