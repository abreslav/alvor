package ee.stacc.productivity.edsl.tracker;

import java.lang.reflect.Modifier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import ee.stacc.productivity.edsl.crawler.ASTUtil;
import ee.stacc.productivity.edsl.crawler.UnsupportedStringOpEx;

/*
 * getLastReachingModIn* methods stay in given scope
 * getLastReachingMod also goes up if needed 
 */

public class VarTrack {
	public static NameUsage getLastMod(Name name) {
		IVariableBinding var = (IVariableBinding)name.resolveBinding();
		if (var.isField() && (var.getModifiers() & Modifier.FINAL) == 0) {
			throw new UnsupportedStringOpEx("Non-final fields are not supported");
		}
		return getLastReachingMod(var, name);
	}
	
	private static NameUsage getLastReachingMod(IVariableBinding var, ASTNode target) {
		NameUsage usage = getLastReachingModIn(var, target, target.getParent());
		if (usage != null) {
			return usage;
		}
		else {
			return getLastReachingMod(var, target.getParent());
		}
	}
	
	private static NameUsage getLastModIn(IVariableBinding var, ASTNode scope) {
		return getLastReachingModIn(var, null, scope);
	}
	
	private static NameUsage getLastReachingModIn(IVariableBinding var, ASTNode target, ASTNode scope) {
		if (isSimpleNode(scope)) {
			return null;			
		}
	    else if (scope instanceof Assignment) {
			return getLastReachingModInAss(var, target, (Assignment)scope);
		}
	    else if (scope instanceof VariableDeclarationStatement) {
	    	return getLastReachingModInVDeclStmt(var, target, (VariableDeclarationStatement)scope);
	    }
		else if (scope instanceof IfStatement) {
			return getLastReachingModInIf(var, target, (IfStatement)scope);
		}
		else if (scope instanceof MethodInvocation) {
			return getLastReachingModInInv(var, target, (MethodInvocation)scope);
		}
		else if (scope instanceof Block) {
			return getLastReachingModInBlock(var, target, (Block)scope);
		}
		else if (scope instanceof InfixExpression) {
			return getLastReachingModInInfix(var, target, (InfixExpression)scope);
		}
		else if (scope instanceof ExpressionStatement) {
			if (target == null) {
				return getLastModIn(var, ((ExpressionStatement)scope).getExpression());
			} 
			else {
				assert (target == ((ExpressionStatement)scope).getExpression());
				return null;
			}
		}
		else if (isLoopStatement(scope)) {
			return getLastReachingModInLoop(var, target, (Statement)scope);
		}
		else {
			throw new UnsupportedStringOpEx("getLastReachingModIn " + scope.getClass());
		}
	}

	private static NameUsage getLastReachingModInInfix(IVariableBinding var,
			ASTNode target, InfixExpression inf) {
		int opIdx;
		if (target == null) {
			opIdx = inf.extendedOperands().size()-1;
		} 
		else {
			opIdx = inf.extendedOperands().indexOf(target)-1;
		}
		for (int i = opIdx; i >= 0; i--) {
			NameUsage usage = getLastModIn(var, (Expression)inf.extendedOperands().get(i));
			if (usage != null) {
				return usage;
			}
		}
		if (target == null || opIdx > -1) {
			NameUsage usage = getLastModIn(var, inf.getRightOperand());
			if (usage != null) {
				return usage;
			}
		}
		if (target == null) {
			NameUsage usage = getLastModIn(var, inf.getLeftOperand());
			if (usage != null) {
				return usage;
			}
		}
		return null;
	}

	private static NameUsage getLastReachingModInLoop(IVariableBinding var,
			ASTNode target, Statement loop) {
		if (target == getLoopBody(loop)) {
			// FIXME check also loop header
			return null;
		}
		else {
			return getLastModIn(var, getLoopBody(loop));
		}
	}

	private static NameUsage getLastReachingModInBlock(IVariableBinding var,
			ASTNode target, Block block) {
		int stmtIdx; // last statement that can affect target
		
		if (target == null) {
			stmtIdx = block.statements().size()-1;
		}
		else {
			stmtIdx = block.statements().indexOf(target)-1;
		}
		
		for (int i = stmtIdx; i >= 0; i--) {
			NameUsage usage = getLastModIn(var, (Statement)block.statements().get(i));
			if (usage != null) {
				return usage;
			}
		}
		
		// no (preceding) statement modifies var, eg. this block doesn't affect target
		return null;
	}

	private static NameUsage getLastReachingModInInv(IVariableBinding var,
			ASTNode target, MethodInvocation inv) {
		if (target == null) {
			// TODO check the effect of method call
			throw new UnsupportedStringOpEx("method call");
		}
		
		int argIdx; // last argument expression that can affect target
		if (target == null) { // all arguments are of interest
			argIdx = inv.arguments().size()-1;
		}
		else { // interested in preceding arguments (if any)
			argIdx = inv.arguments().indexOf(target)-1;
		}
		for (int i = argIdx; i >= 0; i--) {
			NameUsage usage = getLastModIn(var, (Expression)inv.arguments().get(i));
			if (usage != null) {
				return usage;
			}
		}
		
		// FIXME ignoring expressions for now
		return null;
	}

	// get last modification in node that can affect limit
	private static NameUsage getLastReachingModInAss(IVariableBinding var, 
			ASTNode target, Assignment ass) {
		
		if (target == null 
				&& ASTUtil.sameBinding(ass.getLeftHandSide(), var)) {
			return new NameAssignment(ass.getOperator(), ass.getRightHandSide());
		}
		
		// left hand side gets evaluated before rhs
		if (target == null || target == ass.getRightHandSide()) {
			return getLastModIn(var, ass.getLeftHandSide());
		}
		
		return null; 
	}

	private static NameUsage getLastReachingModInVDeclStmt(IVariableBinding var, 
			ASTNode target, VariableDeclarationStatement vDeclStmt) {
		assert vDeclStmt.fragments().size() == 1; // FIXME
		return getLastReachingModInVDecl(var, target, (VariableDeclaration)vDeclStmt.fragments().get(0));
	}
	
	private static NameUsage getLastReachingModInVDecl(IVariableBinding var,
			ASTNode target, VariableDeclaration decl) {
		// FIXME possible to modify smth in initializer 
		if (target == decl.getInitializer()) {
			return null;
		}
		
		if (ASTUtil.sameBinding(decl.getName(), var)) {
			return new NameAssignment(Assignment.Operator.ASSIGN,
						decl.getInitializer());
		}
		else {
			return null;
		}
	}

	private static NameUsage getLastReachingModInIf(IVariableBinding var, ASTNode target, IfStatement ifStmt) {
		
		if (target == null) {
			NameUsage thenUsage = getLastModIn(var, ifStmt.getThenStatement());
			NameUsage elseUsage = null;
			if (ifStmt.getElseStatement() != null) {
				elseUsage = getLastModIn(var, ifStmt.getElseStatement());
			}	
			
			if (thenUsage == null && elseUsage == null) {
				return null;
			}
			else {
				return new NameUsageChoice(thenUsage, elseUsage);
			}
		}
		else {
			return null;
			// FIXME following is more correct
			//assert target == ifStmt.getThenStatement() || target == ifStmt.getElseStatement();
			//return getPrevReachingModIn(var, null, ifStmt.getExpression());
		}
	}
	
	private static boolean isLoopStatement(ASTNode node) {
		return node instanceof WhileStatement
			|| node instanceof ForStatement
			|| node instanceof EnhancedForStatement
			|| node instanceof DoStatement; 
	}
	
	private static Statement getLoopBody(ASTNode loop) {
		if (loop instanceof ForStatement) {
			return ((ForStatement)loop).getBody();
		}
		else if (loop instanceof EnhancedForStatement) {
			return ((EnhancedForStatement)loop).getBody();
		}
		else if (loop instanceof WhileStatement) {
			return ((WhileStatement)loop).getBody();
		}
		else if (loop instanceof DoStatement) {
			return ((DoStatement)loop).getBody();
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	private static boolean isSimpleNode(ASTNode node) {
		return node instanceof Name
			|| node instanceof NullLiteral
			|| node instanceof NumberLiteral
			|| node instanceof StringLiteral
			|| node instanceof BooleanLiteral
			|| node instanceof CharacterLiteral
			|| node instanceof TypeLiteral
			|| node instanceof Annotation
			|| node instanceof ThisExpression;
	}
	
}
