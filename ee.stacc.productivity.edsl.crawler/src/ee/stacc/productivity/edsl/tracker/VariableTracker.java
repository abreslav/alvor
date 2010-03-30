package ee.stacc.productivity.edsl.tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.crawler.ASTUtil;
import ee.stacc.productivity.edsl.crawler.AbstractStringEvaluator;
import ee.stacc.productivity.edsl.crawler.NodeRequest;
import ee.stacc.productivity.edsl.crawler.PositionUtil;
import ee.stacc.productivity.edsl.crawler.UnsupportedStringOpEx;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringChoice;



/**
 * @author Aivar
 *
 */
public class VariableTracker {
	static boolean onlyAssignments=true;
	
	public static List<NameUsage> getPrecedingOccurrences(Name name) {
		ASTUtil.getContainingStmt(name);
		// check if it's used in same expression
		return null;
	}
	
	public static List<NameUsage> createList(NameUsage usage) {
		ArrayList<NameUsage> list = new ArrayList<NameUsage>();
		list.add(usage);
		
		return list;		
	}
	
	private NameUsage getUsageInExpression(Name name, Expression exp) {
		// investigate this expression for this name 
		return null;
	}
	
	private NameUsage getUsageInStatement(Name name, Statement stmt) {
		if (stmt instanceof ExpressionStatement) {
			Expression expr = ((ExpressionStatement)stmt).getExpression(); 
			if (expr instanceof Assignment) {
				Assignment ass = (Assignment)expr;
				NameUsage rhsUsage = getUsageInExpression(name, ass.getRightHandSide());
				if (rhsUsage != null) {
					return rhsUsage;
				}
				// TODO is it possible to modify smth inside LHS expression of assignment? 
				if (ASTUtil.sameBinding(ass.getLeftHandSide(), name)) {
					return new NameAssignment(ass.getOperator(), ass.getRightHandSide());
				}
			}
			else if (onlyAssignments) {
				return null;
			}
			/*
			else if (expr instanceof MethodInvocation) {
				return evalVarAfterMethodInvStmt(name, (ExpressionStatement)stmt);
			}
			*/
			else {
				throw new UnsupportedStringOpEx
					("getVarValAfter(_, ExpressionStatement." + expr.getClass() + ")");
			}
		}
		else if (stmt instanceof VariableDeclarationStatement) {
			VariableDeclaration decl = ASTUtil.getVarDeclFragment
				((VariableDeclarationStatement)stmt, name);
			if (decl != null) {
				return new NameAssignment(Assignment.Operator.ASSIGN, decl.getInitializer());
			}
			else {
				return null; 
			}
		}
		/*
		else if (stmt instanceof IfStatement) {
			return evalVarAfterIf(name, (IfStatement)stmt);
		}
		else if (stmt instanceof Block) {
			return evalVarAfter(name, ASTUtil.getLastStmt((Block)stmt));
		}
		else if (stmt instanceof ReturnStatement) {
			return evalVarBefore(name, stmt);
		}
		else if (stmt instanceof ForStatement) {
			return evalVarAfterFor(name, (ForStatement)stmt);
		}
		else { // other kind of statement
			throw new UnsupportedStringOpEx("getVarValAfter(var, " + stmt.getClass().getName() + ")");
		} 
		*/
		return null;
	}
	
	private List<NameUsage> getPrecedingOccurrences(Name name, Statement stmt) {
		IVariableBinding var = (IVariableBinding) name.resolveBinding();
		Statement prevStmt = ASTUtil.getPrevStmt(stmt);
		
		// TODO if at the boundary of loop then ...
		/* 
		if (prevStmt == null) {
			// no previous statement, must be beginning of method declaration
			if (var.isField()) {
				return createList(new AssignmentToName(..., paramIndex));
			}
			else if (var.isParameter()) {
				MethodDeclaration method = ASTUtil.getContainingMethodDeclaration(stmt);
				int paramIndex = ASTUtil.getParamIndex(method, var);
				return createList(new NameAsParameter(..., paramIndex));
				
			}
			else {
				throw new UnsupportedStringOpEx
					("getVarValBefore: not param, not field, kind=" + var.getKind());
			}
		}
		else {
			return evalVarAfter(name, prevStmt);
		}
		*/
		return null;
	}
	
	
}
