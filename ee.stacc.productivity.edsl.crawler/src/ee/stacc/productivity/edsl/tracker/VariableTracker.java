package ee.stacc.productivity.edsl.tracker;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

import ee.stacc.productivity.edsl.crawler.ASTUtil;
import ee.stacc.productivity.edsl.crawler.UnsupportedStringOpEx;



/**
 * @author Aivar


Aliasing problem:
	
	StringBuffer b = new StringBuffer();
	makeAlias(b); // this is detected as possible modification place
	
	b.append();
	modifyUsingAlias(); // this modification place of b is not detected (when moving upwards)
	
	hotspot(b);

 */
public class VariableTracker {
	static boolean onlyAssignments=true;
	
	public static NameUsage getPreviousUsage(Name name) {
		return null;
	}
	
	public static List<NameUsage> getPreviousModfications(Name name) {
		return getUsagesBefore((IVariableBinding) name.resolveBinding(), name);
	}
	
	/*
	private static NameUsage getUsageInExpression(Name name, Expression exp) {
		// investigate this expression for this name 
		return null;
	}
	
	private static NameUsage getUsageInStatement(Name name, Statement stmt) {
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
			else if (expr instanceof MethodInvocation) {
				return evalVarAfterMethodInvStmt(name, (ExpressionStatement)stmt);
			}
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
		return null;
	}
	*/
	
	private static List<Expression> getPreviousExpressions(ASTNode node) {
		ASTNode parent = node.getParent();
		if (parent == null) {
			return asList();
		}
		else if (parent instanceof Assignment) {
			Assignment ass = (Assignment)parent;
			if (node == ass.getRightHandSide()) {
				return asList(ass.getLeftHandSide());
			}
			else {
				return getPreviousExpressions(ass);
			}
		}
		else if (parent instanceof InfixExpression) {
			//throw new UnsupportedConstructionException("TODO");
		}
		else if (parent instanceof ExpressionStatement) {
			// TODO get last expression of the previous statement
			//return getPrecedingExpression(var, ASTUtil.getPrevStmt((Statement)parent));
		}
		else if (parent instanceof WhileStatement) {
			WhileStatement wStmt = (WhileStatement)parent;
			// return last expr of the same loop body
			// plus
			// FIXME ignoring loop condition expression for nows
			//List<Expression> result = getLastExpressionIn(ASTUtil.getPrevStmt(wStmt));
			//result.addAll(getLastExpressionIn(wStmt));
			
			return null;			
		}
		else if (parent instanceof MethodInvocation) {
			MethodInvocation inv = (MethodInvocation)parent;
			// expression is first thing to be evaluated in invocation
			if (node == inv.getExpression()) {
				return getPreviousExpressions(inv);
			}
			else if (node == inv.getName()) {
				// this case probably is not used, but let it be, for completeness
				return asList(inv.getExpression());
			}
			else { // node must be one of arguments
				int argIndex = inv.arguments().indexOf(node);
				assert argIndex > -1;
				if (argIndex == 0) {
					return asList(inv.getExpression());
				}
				else {
					return asList((Expression)inv.arguments().get(argIndex-1));
				}
			}
		}
		throw new UnsupportedConstructionException("getPrecedingExpression - node: " + node.getClass()
				+ ", parent:" + parent.getClass());
	}
	
	
	/*
	private static List<Expression> _getLastExpressionIn(ASTNode node) {
		return null;
	}
	*/
	
	// used for moving left in AST
	private static ASTNode getPreviousSibling(ASTNode node) {
		return null;
	}
	
	private static NameUsage getLastUsageIn(IVariableBinding var, ASTNode node) {
		if (node instanceof Assignment) {
			Assignment ass = (Assignment)node;
			if (ASTUtil.sameBinding(ass.getLeftHandSide(), var)) {
				return new NameAssignment(ass.getOperator(), ass.getRightHandSide());
			}
			else {
				return getLastUsageIn(var, ass.getRightHandSide());
			}
		}
		else if (node instanceof IfStatement) {
			IfStatement ifStmt = (IfStatement)node;
			NameUsage thenUsage = getLastUsageIn(var, ifStmt.getThenStatement());
			NameUsage elseUsage = null;
			if (ifStmt.getElseStatement() != null) {
				elseUsage = getLastUsageIn(var, ifStmt.getElseStatement());
			}	
			
			if (thenUsage == null && elseUsage == null) {
				return null;
			}
			else {
				return new NameUsageChoice(thenUsage, elseUsage);
			}
		}
		else if (isLoopStatement(node)) {
			return getLastUsageIn(var, getLoopBody(node));
		}
		else if (node instanceof Block) {
			Statement stmt = ASTUtil.getLastStmt((Block)node);
			if (stmt != null) {
				return getLastUsageIn(var, stmt);
			}
			else {
				return null;
			}
		}
		else if (node instanceof ExpressionStatement) {
			return getLastUsageIn(var, ((ExpressionStatement)node).getExpression());
		}
		else {
			throw new UnsupportedStringOpEx("getLastUsageIn " + node.getClass());
		}
	}
	
	private static NameUsage getUsageBefore(IVariableBinding var, ASTNode node) {
		ASTNode sibling = node;
		while ((sibling = getPreviousSibling(sibling)) != null) {
			NameUsage usage = getLastUsageIn(var, sibling);
			if (usage != null) {
				if (isLoopStatement(sibling)) {
					// include also usage before loop because loop may not execute
					return new NameUsageChoice(usage, getUsageBefore(var, sibling));
				}
				return usage;
			}
		}
		// no usage in siblings, go up
		ASTNode parent = node.getParent();
		NameUsage prevUsage = getUsageBefore(var, node.getParent());
		
		if (isLoopStatement(parent)) { // ie. coming out of loop
			// go look if this name was modded in loop body
			// yes, in some cases it means duplicate work
			// but it should be remedied by cache
			NameUsage loopUsage = getLastUsageIn(var, parent);
			if (loopUsage != null) {
				return new NameUsageLoopChoice(prevUsage, loopUsage);
			}
		}
		
		return prevUsage;
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
	
	private static List<NameUsage> getUsagesBefore(IVariableBinding var, ASTNode node) {
		List<Expression> prevs = getPreviousExpressions(node);
		List<NameUsage> usages = new ArrayList<NameUsage>();
		
		for (Expression expr : prevs) {
			NameUsage usage = getLastUsageIn(var, expr);
			if (usage == null && usages.indexOf(usage) != -1) { // TODO need to implement equals
				usages.add(usage);
			}
			else {
				usages.addAll(getUsagesBefore(var, expr));
			}
		}
		
		return usages;
	}

/*
getPrevSib(node):ASTNode

getNameDefsBefore(var, node) =

    var sib := node
    while sib := getPrevSib(sib) do
        defs := getLastDefinitionsIn(var, sib)
        if defs then
            if sib is Loop then
                add also defs before loop
            return defs
    
    // nothing from siblings
    
    parent = node.parent
    defs = getNameDefsBefore(var, parent)
    
    if parent is loop // means we're getting out of loop, add also defs in loop
        loopDefs = getLastDefsIn(var, parent)
            // should i return normal choice??
            // and later detect that one is in loop and other not?
            // no - LoopChoice keeps distinction between first iteration value and rest
        if loopDefs.notEmpty then
            return DefLoopChoice(defs, loopDefs)
        else
            return defs
    
    else // not loop
        return getNameDefsBefore(var, node.parent)



getLastDefsIn(var, node) =
    if node is IfStatement then
    
    if node is Block then
        return getLastDefsIn(var, node.lastStmt)
    
    if node is WhileStatement then
        return getLastDefsIn(var, node.body)
        
    
        
        
        
 */
}
