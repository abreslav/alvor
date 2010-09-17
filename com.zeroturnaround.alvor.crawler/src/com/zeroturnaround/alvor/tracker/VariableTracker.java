package com.zeroturnaround.alvor.tracker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.zeroturnaround.alvor.cache.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.crawler.ASTUtil;
import com.zeroturnaround.alvor.string.IPosition;

/*
 * getLastReachingModIn* methods stay in given scope
 * getLastReachingMod also goes up if needed 
 */

public class VariableTracker {
	public static NameUsage getLastMod(Name name) {
		IVariableBinding var = (IVariableBinding)name.resolveBinding();
		return getLastReachingMod(var, name);
	}
	
	/**
	 * Finds previous modification place of var, that is preceding target in CFG
	 * @param var 
	 * @param target The node that gonna be affected by the Mod
	 * @return
	 */
	public static NameUsage getLastReachingMod(IVariableBinding var, ASTNode target) {
		assert target != null;
		if (var.isField()) {
			return getFieldDefinition(var);
		}
		
		ASTNode parent = target.getParent();
		NameUsage usage = getLastReachingModIn(var, target, parent);
		if (usage != null) {
			return usage;
		}
		else {
			return getLastReachingMod(var, target.getParent());
		}
	}
	
	
	public static NameUsage getLastModIn(IVariableBinding var, ASTNode scope) {
		if (var.isField()) {
			return getFieldDefinition(var);
		}
		return getLastReachingModIn(var, null, scope);
	}
	
	private static NameUsage getFieldDefinition(IVariableBinding var) {
		// for now, (final)fields should be handled by client 
		throw new UnsupportedStringOpEx("Fields are not supported in tracker", (IPosition)null);
	}
	
	/*
	 * target is a descendant node of scope and we're looking for (possible)modifications to var that
	 * happen before target node is evaluated.
	 * if target == null then whole scope should be searched 
	 */
	private static NameUsage getLastReachingModIn(IVariableBinding var, ASTNode target, ASTNode scope) {
		if (ASTUtil.isSimpleNode(scope)) {
			return null;			
		}
	    else if (scope instanceof Assignment) {
			return getLastReachingModInAss(var, target, (Assignment)scope);
		}
	    else if (scope instanceof VariableDeclarationStatement) {
	    	return getLastReachingModInVDeclStmt(var, target, (VariableDeclarationStatement)scope);
	    }
	    else if (scope instanceof VariableDeclaration) {
	    	return getLastReachingModInVDecl(var, target, (VariableDeclaration)scope);
	    }
		else if (scope instanceof IfStatement) {
			return getLastReachingModInIf(var, target, (IfStatement)scope);
		}
		else if (scope instanceof ConditionalExpression) {
			return getLastReachingModInCondExpr(var, target, (ConditionalExpression)scope);
		}
		else if (scope instanceof MethodInvocation) {
			return getLastReachingModInInv(var, target, (MethodInvocation)scope);
		}
		else if (scope instanceof MethodDeclaration) {
			return getLastReachingModInMethodDecl(var, target, (MethodDeclaration)scope);
		}
		else if (scope instanceof PostfixExpression) {
			return getLastReachingModInPostfixExp(var, target, (PostfixExpression)scope);
		}
		else if (scope instanceof ClassInstanceCreation) { // constructor call
			// TODO it's possible to modify smth in arguments (or expression?)
			return null;
		}
		else if (scope instanceof ArrayAccess) {
			// TODO it's possible to modify smth in index or array expression
			return null;
		}
		else if (scope instanceof Block) {
			return getLastReachingModInBlock(var, target, (Block)scope);
		}
		else if (scope instanceof InfixExpression) {
			return getLastReachingModInInfix(var, target, (InfixExpression)scope);
		}
		else if (scope instanceof ReturnStatement) {
			return getLastReachingModInReturn(var, target, (ReturnStatement)scope);
		}
		else if (scope instanceof TryStatement) {
			return getLastReachingModInTry(var, target, (TryStatement)scope);
		}
		else if (scope instanceof ThrowStatement) {
			return getLastReachingModInThrow(var, target, (ThrowStatement)scope);
		}
		else if (scope instanceof CatchClause) {
			return getLastReachingModInCatchClause(var, target, (CatchClause)scope);
		}
		else if (scope instanceof CastExpression) {
			return null; // FIXME
		}
		else if (scope instanceof ParenthesizedExpression) {
			if (target == null) {
				return getLastReachingModIn(var, target, 
						((ParenthesizedExpression)scope).getExpression());
			}
			else {
				return null;
			}
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
		else if (ASTUtil.isLoopStatement(scope)) {
			return getLastReachingModInLoop(var, target, (Statement)scope);
		}
		else if (scope instanceof EmptyStatement) {
			return null;
		}
		else {
			throw new UnsupportedStringOpEx("getLastReachingModIn " + scope.getClass(), scope);
		}
	}

	private static NameUsage getLastReachingModInCondExpr(IVariableBinding var,
			ASTNode target, ConditionalExpression condExpr) {
		
		if (target == null) {
			NameUsage thenUsage = getLastModIn(var, condExpr.getThenExpression());
			NameUsage elseUsage = null;
			if (condExpr.getElseExpression() != null) {
				elseUsage = getLastModIn(var, condExpr.getElseExpression());
			}
			
			if (thenUsage == null && elseUsage == null) {
				return null;
			}
			
			
			return new NameUsageChoice(condExpr, thenUsage, elseUsage);
		}
		else {
			return null;
			// FIXME check also inside header
			//assert target == ifStmt.getThenStatement() || target == ifStmt.getElseStatement();
			//return getPrevReachingModIn(var, null, ifStmt.getExpression());
		}
	}

	private static NameUsage getLastReachingModInCatchClause (
			IVariableBinding var, ASTNode target, CatchClause scope) {
		if (target == null) {
			// should check also header ??
			return getLastModIn(var, scope.getBody());
		}
		else {
			assert (target == scope.getBody());
			return null;
		}
	}

	private static NameUsage getLastReachingModInThrow(IVariableBinding var,
			ASTNode target, ThrowStatement stmt) {
		if (target == null) {
			return getLastModIn(var, stmt.getExpression());
		}
		return null;
	}

	private static NameUsage getLastReachingModInPostfixExp(
			IVariableBinding var, ASTNode target, PostfixExpression postfix) {
		// ++ or --
		if (target == null) {
			if (ASTUtil.sameBinding(postfix.getOperand(), var)) {
				throw new UnsupportedStringOpEx("Postfix operand", postfix);
			}
		}
		return getLastReachingModIn(var, target, postfix.getOperand());
	}

	private static NameUsage getLastReachingModInTry(IVariableBinding var,
			ASTNode target, TryStatement tryStmt) {
		if (target == null) {
			// TODO should check also catches and finally
			return getLastModIn(var, tryStmt.getBody());
		}
		else {
			//assert (target == tryStmt.getBody());
			// FIXME assuming no changes in catch and finally clauses
			return null;
		}
	}

	private static NameUsage getLastReachingModInReturn(IVariableBinding var,
			ASTNode target, ReturnStatement ret) {
		if (target == null && ret.getExpression() != null) {
			return getLastReachingMod(var, ret.getExpression());
		}
		else {
			return null;
		}
	}

	private static NameUsage getLastReachingModInMethodDecl(
			IVariableBinding var, ASTNode target, MethodDeclaration decl) {
		assert target == null ||  target == decl.getBody();
		
		if (target == null) {
			return getLastModIn(var, decl.getBody());
		}
		else {
			assert target == decl.getBody();
			if (!var.isParameter()) {
				throw new UnsupportedStringOpEx("Non-parameter var ("
						+ var+ ") asked from MethodDeclaration ("
						+ decl.getName().getFullyQualifiedName() + ")", decl); 
			}
			
			int idx = ASTUtil.getParamIndex0(decl, var);
			assert(idx != -1);
//			if (idx == -1) {
//				throw new UnsupportedStringOpEx("Parameter ("
//						+ var+ ") not found in ("
//						+ decl.getName().getFullyQualifiedName() + ")"); 
//			}
			return new NameInParameter(decl, idx);
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
		if (target == ASTUtil.getLoopBody(loop)) {
			// FIXME check also loop header
			return null;
		}
		else {
			NameUsage usage = getLastModIn(var, ASTUtil.getLoopBody(loop));
			return usage;
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
			Statement stmt = (Statement)block.statements().get(i);
			
			NameUsage usage = getLastModIn(var, stmt);
			if (usage != null) {
				// if target is loop then moving out of loop??
				
				// stmt is loop then found last usage in loop
				
				// FIXME won't work in all cases
				// should create new Tracker at loop entry
				// and another in loop exit ???
				
				
				if (ASTUtil.isLoopStatement(target)) { // moving out of loop
					assert(ASTUtil.getLoopBody(target) != block);
//					throw new UnsupportedConstructionException("target is loop");
					return new NameUsageLoopChoice(target, usage, 
							getLastModIn(var, ASTUtil.getLoopBody(target)));
				}
				else {
					return usage;
				}
			}
		}
		
		// no (preceding) statement modifies var, e.g. this block doesn't affect target
		return null;
	}

	private static NameUsage getLastReachingModInInv(IVariableBinding var,
			ASTNode target, MethodInvocation inv) {
		
		// optimize for immutable types
		if (ASTUtil.isString(var.getType())) {
			return null;
		}
		
//		// TODO: too bold statement, but speeds up things
//		if (inv.getName().getIdentifier().equals("toString")) {
//			return null;
//		}
		
		if (target == null) { // check the effect of evaluating the method call
			// TODO if name is at more than 1 position, then this means trouble (because of aliasing)
			
			// check in expression position
			// NB! this is relevant place for sb.append(...).append(...)
			Expression exp = inv.getExpression();
			if (exp != null && ASTUtil.varIsUsedIn(var, exp)) {
				return new NameInMethodCallExpression(inv, exp);
			}
			
			
			// check in arguments
			// special case: StringBuffer methods don't modify their arguments 
			if (exp != null 
					&& (
							ASTUtil.isStringBuilderOrBuffer(exp.resolveTypeBinding())
							|| exp.resolveTypeBinding().getQualifiedName().equals("java.lang.Integer"))
					) {
				return null;
			}
			
			for (int i = 0; i < inv.arguments().size(); i++) {
				Expression arg = (Expression)inv.arguments().get(i);
				if (returnsVarPointer(arg, var)) {
					return new NameInArgument(inv, i);
				}
			}
		}
		
		// check the effect of evaluating arguments (and TODO method expression)
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
		
		// FIXME ignoring possible modifications in expression for now
		return null;
	}
	
	private static boolean returnsVarPointer(Expression exp, IVariableBinding var) {
		if (exp instanceof Name) {
			return ASTUtil.sameBinding(exp, var);
		}
		else if (exp instanceof CastExpression) {
			return returnsVarPointer(((CastExpression)exp).getExpression(), var);
		}
		else if (exp instanceof ParenthesizedExpression) {
			return returnsVarPointer(((ParenthesizedExpression)exp).getExpression(), var);
		}
		else {
			return false; // TODO anything else?
		}
	}

	// get last modification in node that can affect limit
	private static NameUsage getLastReachingModInAss(IVariableBinding var, 
			ASTNode target, Assignment ass) {
		
		if (target == null 
				&& ASTUtil.sameBinding(ass.getLeftHandSide(), var)) {
			return new NameAssignment(ass);
		}
		
		// left hand side gets evaluated before rhs
//		if (target == null || target == ass.getRightHandSide()) {
//			return getLastModIn(var, ass.getLeftHandSide());
//		}
		
		return null; 
	}

	private static NameUsage getLastReachingModInVDeclStmt(IVariableBinding var, 
			ASTNode target, VariableDeclarationStatement vDeclStmt) {
		
		int fragIdx; 
		if (target == null) { // all fragments are of interest
			fragIdx = vDeclStmt.fragments().size()-1;
		}
		else { // interested in preceding fragments (if any)
			fragIdx = vDeclStmt.fragments().indexOf(target)-1;
		}
		for (int i = fragIdx; i >= 0; i--) {
			NameUsage usage = getLastModIn(var, 
					(VariableDeclarationFragment)vDeclStmt.fragments().get(i));
			if (usage != null) {
				return usage;
			}
		}
		
		return null;
//		return getLastReachingModInVDecl(var, target, (VariableDeclaration)vDeclStmt.fragments().get(0));
	}
	
	private static NameUsage getLastReachingModInVDecl(IVariableBinding var,
			ASTNode target, VariableDeclaration decl) {
		// FIXME possible to modify smth in initializer 
		if (target == decl.getInitializer()) {
			return null;
		}
		
		if (ASTUtil.sameBinding(decl.getName(), var)) {
			return new NameAssignment(decl);
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
			
			
			return new NameUsageChoice(ifStmt, thenUsage, elseUsage);
		}
		else {
			return null;
			// FIXME check also inside header
			//assert target == ifStmt.getThenStatement() || target == ifStmt.getElseStatement();
			//return getPrevReachingModIn(var, null, ifStmt.getExpression());
		}
	}
}
