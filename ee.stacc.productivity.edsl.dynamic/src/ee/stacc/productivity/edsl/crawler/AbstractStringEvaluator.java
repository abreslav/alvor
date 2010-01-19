package ee.stacc.productivity.edsl.crawler;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringSequence;


public class AbstractStringEvaluator {
	
	public static Statement getPrevStmt(Statement node) {
		//System.out.println("getPrevStmt: " + node.getClass().getName());
		
		if (node.getParent() instanceof Block) {
			Block block = (Block) node.getParent();
			int i = block.statements().indexOf(node);
			
			if (i == 0) { // this is first in block, eg. this block is done
				return getPrevStmt(block);
			} else {
				return (Statement)block.statements().get(i-1);
			}
		} 
		else if (node.getParent() instanceof MethodDeclaration) {
			return null;
		}
		else if (node.getParent() instanceof IfStatement) {
			return getPrevStmt((IfStatement)node.getParent());
		}
		else { 
			throw new UnsupportedStringOpEx("TODO: getPrevStatement(" + node.getClass().getName() 
				+ ", parent is " + node.getParent().getClass().getName() + ")");
		}
	}
	
	
	public static Statement getContainingStmt(ASTNode node) {
		assert node != null;
		
		if (node.getParent() instanceof Statement) {
			return (Statement)node.getParent();
		}
		else {
			return getContainingStmt(node.getParent());
		}
	}
	
	private static IAbstractString getVarValAfterIf(IBinding var, IfStatement stmt) {
		IAbstractString ifVal = getVarValAfter(var, stmt.getThenStatement());
		IAbstractString elseVal = null;
		
		if (stmt.getElseStatement() != null) {
			elseVal = getVarValAfter(var, stmt.getElseStatement());
		} else {
			elseVal = getVarValBefore(var, stmt);
		}
		
		if (ifVal.equals(elseVal)) {
			return ifVal;
		} else {
			return new StringChoice(ifVal, elseVal);
		}
	}
	
	private static IAbstractString getVarValAfterVarDec(IBinding var,
			VariableDeclarationStatement stmt) {
		assert stmt.fragments().size() == 1; // TODO
		VariableDeclaration vdec = (VariableDeclaration)stmt.fragments().get(0);
		
		if (vdec.getName().resolveBinding() == var) {
			return getValOf(vdec.getInitializer());
		}
		else {
			return getVarValBefore(var, stmt);
		}
	}
	
	private static IAbstractString getVarValAfterAss(IBinding var, 
			ExpressionStatement stmt) {
		assert stmt.getExpression() instanceof Assignment;
		
		Assignment ass = (Assignment)stmt.getExpression();
		
		if (ass.getLeftHandSide() instanceof SimpleName
			&& ((SimpleName)ass.getLeftHandSide()).resolveBinding() == var) {
			
			IAbstractString rhs = getValOf(ass.getRightHandSide());
			
			if (ass.getOperator() == Assignment.Operator.ASSIGN) {
				return rhs;
			}
			else if (ass.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
				return new StringSequence(getVarValBefore(var, stmt), rhs);
			}
			else {
				throw new UnsupportedStringOpEx("getVarValAfterAss: unknown operator");
			}
		}
		else { // wrong assignment, this statement doesn't change var (hopefully :)
			return getVarValBefore(var, stmt);
		}
	}
	
	public static IAbstractString getVarValAfter(IBinding var, Statement stmt) {
		//System.out.println("getVarValAfter: var=" + var.getName()
		//		+ ", stmt="+ stmt.getClass().getName());
		
		if (stmt instanceof ExpressionStatement) {
			if (((ExpressionStatement)stmt).getExpression() instanceof Assignment) {
				return getVarValAfterAss(var, (ExpressionStatement)stmt);
			}
			else {
				return getVarValBefore(var, stmt);
			}
		}
		else if (stmt instanceof VariableDeclarationStatement) {
			return getVarValAfterVarDec(var, (VariableDeclarationStatement)stmt);
		}
		else if (stmt instanceof IfStatement) {
			return getVarValAfterIf(var, (IfStatement)stmt);
		}
		else if (stmt instanceof Block) {
			return getVarValAfter(var, getLastStmt((Block)stmt));
		}
		else { // other kind of statement
			throw new UnsupportedStringOpEx("TODO: getVarValAfter(_, " + stmt.getClass().getName() + ")");
		} 
	}
	
	public static Statement getLastStmt(Block block) {
		return (Statement)block.statements().get(block.statements().size()-1);
	}
	
	public static IAbstractString getVarValBefore(IBinding var, Statement stmt) {
		//System.out.println("getVarValBefore: var=" + var.getName()
		//		+ ", stmt="+ stmt.getClass().getName());
		
		Statement prevStmt = getPrevStmt(stmt);
		if (prevStmt == null) {
			// no prev statement, must be beginning of method declaration
			// and value should come from parameter
			throw new UnsupportedStringOpEx("TODO: analyze argument '" + var.getName() 
					+ "' at all call sites");
			
			// find all this method invocations
			// create choice of each invo
		}
		else {
			return getVarValAfter(var, prevStmt);
		}
	}
	
	public static IAbstractString getValOf(Expression node) {
		ITypeBinding type = node.resolveTypeBinding();
		/*
		System.out.println("getValOf: class=" + node.getClass().getSimpleName()
				+ ", toString=" + node.toString()
				+ ", binding=" + type.getName());
		*/
		
		if (type.getName().equals("int")) {
			throw new UnsupportedStringOpEx
				("TODO: int expression");
		}
		else if (node instanceof SimpleName) {
			return getVarValBefore(((SimpleName)node).resolveBinding(),
				getContainingStmt(node));
		}
		else if (node instanceof StringLiteral) {
			return new StringConstant(((StringLiteral)node).getLiteralValue());
		}
		else if (node instanceof ParenthesizedExpression) {
			return getValOf(((ParenthesizedExpression)node).getExpression());
		}
		else if (node instanceof InfixExpression) {
			InfixExpression infe = (InfixExpression)node;
			if (infe.getOperator() == InfixExpression.Operator.PLUS) {
				return new StringSequence(
					getValOf(infe.getLeftOperand()),
					getValOf(infe.getRightOperand()));
			}
			else {
				throw new UnsupportedStringOpEx
					("TODO: getValOf( infix op = " + infe.getOperator() + ")");
			}
		}
		// StringBuilder.toString()
		else if (node instanceof MethodInvocation) {
			MethodInvocation inv = (MethodInvocation)node;
			if (!(inv.getExpression() instanceof SimpleName)) {
				throw new UnsupportedStringOpEx("MethodInvocation, expression not SimpleName");
			}
			if (! "toString".equals(inv.getName().getIdentifier())) {
				throw new UnsupportedStringOpEx("MethodInvocation, method not toString");
			}
			ITypeBinding binding = ((SimpleName)inv.getExpression()).resolveTypeBinding();
			if (! "java.lang.StringBuffer".equals(binding.getQualifiedName())
				&& ! "java.lang.StringBuilder".equals(binding.getQualifiedName())) {
				throw new UnsupportedStringOpEx("MethodInvocation, neither StringBuffer nor StringBuilder");
			}
			return null; // TODO
		}
		else {
			throw new UnsupportedStringOpEx
				("TODO: getValOf(" + node.getClass().getName() + ")");
		}
	}
	
	static IFile getNodeFile(ASTNode node) {
		CompilationUnit unit = (CompilationUnit)node.getRoot();
		return (IFile)unit.getTypeRoot().getResource();
	}

}