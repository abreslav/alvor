package com.zeroturnaround.alvor.tracker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
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
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.zeroturnaround.alvor.common.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.crawler.util.UnsupportedStringOpExAtNode;
import com.zeroturnaround.alvor.string.IPosition;

/*
 * getLastReachingModIn* methods stay in given scope
 * getLastReachingMod also goes up if needed 
 */

public class VariableTracker {
	private static final ILog LOG = Logs.getLog(VariableTracker.class);
	public static NameUsage getLastMod(Name name) {
		IVariableBinding var = (IVariableBinding)name.resolveBinding();
		return getLastReachingMod(var, name);
	}
	
	/**
	 * Finds previous modification place of var, that is preceding target in CFG
	 * @param var 
	 * @param target - modifications are searched backwards from this node
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
			
			// nothing found inside parent, so search in things preceding the parent
			NameUsage precedingUsage = getLastReachingMod(var, parent);
			
			/*
			 *  A special case:
			 *  if parent is a loop, then target is something inside a loop
			 *    - mod was not found between target and start of the loop
			 *    - normal action is to start moving upwards from the parent
			 *    - in loop case you also need to check, if preceding loop iterations could modify var
			 *    
			 *  So look again for modification in loop-s body, if find something then make a choice of 
			 *  that and stuff found preceding the loop. (Client should take care of avoiding infinite recursion)
			 *  
			 */
			if (ASTUtil.isLoopStatement(parent)) {
				// special case -- we're at the loop boundary, "preceding" gets another meaning:
				// 		need also to take into account the preceding iteration of the loop 
				NameUsage loopUsage = getLastModIn(var, ASTUtil.getLoopBody(parent));
				if (loopUsage != null) {
					precedingUsage = new NameUsageChoice(parent, precedingUsage, loopUsage);
				}
			} 
			
			/*
			 * TODO Another special case: if parent is if-statement, then check whether
			 * this var was mentioned in the condition and mention that condition in the usage,
			 * so that StringEvaluator can filter result
			 */
			
			else if (parent instanceof IfStatement
					&& conditionRequiresNonNullVariable(((IfStatement)parent).getExpression(), var)) {
				precedingUsage = new UsageFilter(precedingUsage, true);
			}

			return precedingUsage;			
		}
	}
	
	private static boolean conditionRequiresNonNullVariable(Expression condition, IVariableBinding var) {
		assert (condition.resolveTypeBinding().getName().equals("boolean"));
		// TODO yes, it's gross simplification
		if (condition instanceof InfixExpression) {
			InfixExpression infix = (InfixExpression)condition;
			Expression left = infix.getLeftOperand();
			Expression right = infix.getRightOperand();
			if (infix.getOperator() == InfixExpression.Operator.NOT_EQUALS
					&& left instanceof Name
					&& ((Name)left).resolveBinding().isEqualTo(var)
					&& right instanceof NullLiteral) {
				return true;
			}
		}
		return false;
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
		else if (scope instanceof PrefixExpression) {
			return getLastReachingModInPrefixExp(var, target, (PrefixExpression)scope);
		}
		else if (scope instanceof PostfixExpression) {
			return getLastReachingModInPostfixExp(var, target, (PostfixExpression)scope);
		}
		else if (scope instanceof SwitchStatement) {
			return getLastReachingModInSwitch(var, target, (SwitchStatement)scope);
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
		else if (scope instanceof BreakStatement) {
			return null;
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
				return getLastReachingModIn(var, null, 
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
			throw new UnsupportedStringOpExAtNode("getLastReachingModIn " + scope.getClass(), scope);
		}
	}

	private static NameUsage getLastReachingModInSwitch(IVariableBinding var,
			ASTNode target, SwitchStatement scope) {
		
		if (target == null) {
			// Locate all breaks plus last case (ie. exit points from switch).
			// From each start moving upwards (until preceding break or start of the switch)
			// Put all results in a choice
			NameUsage totalUsage = null;
			
			for (int i=0; i < scope.statements().size(); i++) {
				Statement stmt = (Statement)scope.statements().get(i);
				if (stmt instanceof SwitchCase) {
					continue;
				}
				Statement nextStmt = null;
				if (i+1 < scope.statements().size()) {
					nextStmt = (Statement)scope.statements().get(i+1);
				}
				if (nextStmt == null || nextStmt instanceof BreakStatement) {
					NameUsage usage = getLastReachingModIn(var, null, stmt); // search in that statement
					if (usage == null) {
						usage = getLastReachingModInSwitch(var, stmt, scope); // search before that 
					}
					if (usage != null) {
						if (totalUsage == null) {
							totalUsage = usage;
						}
						else {
							totalUsage = new NameUsageChoice(null, totalUsage, usage);
						}
					}
				}
			}
			return totalUsage;
		}
		else {
			// target must be some statement inside switch
			assert target.getParent() == scope;
			// move upwards until break statement
			if (target instanceof BreakStatement) {
				return null;				
			}
			else {
				Statement prevStmt = getPrecedingSwitchStatement(scope, target);
				if (prevStmt == null) {
					return null;
				}
				while (prevStmt instanceof SwitchCase) {
					prevStmt = getPrecedingSwitchStatement(scope, prevStmt);
					if (prevStmt == null) {
						return null;
					}
				}
				NameUsage usage = getLastReachingModIn(var, null, prevStmt);
				if (usage == null) {
					return getLastReachingModInSwitch(var, prevStmt, scope);
				}
				else {
					return usage;
				}
			}
		}
	}
	
	private static Statement getPrecedingSwitchStatement(SwitchStatement switchStmt, ASTNode stmt) {
		int i = switchStmt.statements().indexOf(stmt);
		assert i > -1;
		if (i == 0) {
			return null;
		}
		
		Statement prev = (Statement)switchStmt.statements().get(i-1);
		if (prev instanceof BreakStatement) {
			return null;
		}
		
		if (prev instanceof SwitchCase) {
			// look upwards into previous case, if this doesn't end with break, then it's last statement is preceding
			return getPrecedingSwitchStatement(switchStmt, prev);
		}
		else {
			return prev;
		}
	}

	private static NameUsage getLastReachingModInPrefixExp(
			IVariableBinding var, ASTNode target, PrefixExpression scope) {
		// !, ~, +, -, ++, --
		if (target == null) {
			if (ASTUtil.sameBinding(scope.getOperand(), var)) {
				throw new UnsupportedStringOpExAtNode("Prefix operand", scope);
			}
		}
		return null;
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
				throw new UnsupportedStringOpExAtNode("Postfix operand", postfix);
			}
		}
		return null;
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
		
		NameUsage result = null;
		if (target == null) {
			result = getLastModIn(var, decl.getBody());
		}
		
		if (target != null || result == null) {
			if (!var.isParameter()) {
				throw new UnsupportedStringOpExAtNode("Non-parameter var ("
						+ var+ ") asked from MethodDeclaration ("
						+ decl.getName().getFullyQualifiedName() + ")", decl); 
			}
			
			int idx = ASTUtil.getParamIndex0(decl, var);
			assert(idx != -1);
			return new NameInParameter(decl, idx);
		}
		else {
			return result;
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
			assert target == null;
			NameUsage usage = getLastModIn(var, ASTUtil.getLoopBody(loop));
			
			if (usage == null) {
				return null;
			} 
			else {
				// loop may not be executed, therefore it can be thought to have
				// an hidden empty 'else' branch 
				return new NameUsageChoice(loop, usage, null);
			}
		}
	}

	private static NameUsage getLastReachingModInBlock(IVariableBinding var,
			ASTNode target, Block block) {
		int stmtIdx; // last statement that can affect target
		
		if (target == null) { // search whole block
			stmtIdx = block.statements().size()-1;
		}
		else { 
			// should be called only when block really is target's direct parent
			stmtIdx = block.statements().indexOf(target)-1;
		}
		
		// go backwards in statements
		for (int i = stmtIdx; i >= 0; i--) {
			Statement stmt = (Statement)block.statements().get(i);
			
			NameUsage usage = getLastModIn(var, stmt);
			if (usage != null) {
				return usage;
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
