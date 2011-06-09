package com.googlecode.alvor.conntracker;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.StringLiteral;

import com.googlecode.alvor.crawler.util.ASTUtil;


public class ConnectionTracker {
	public static String getConnectionDescriptor(Expression node) {
		if (node == null) {
			return "";
		}
		else if (node instanceof MethodInvocation) {
			return getPatternStringForInvocation((MethodInvocation)node);
		}
		else if (node instanceof Name) {
			Name name = (Name)node;
			IBinding binding = name.resolveBinding();
			if (binding instanceof IVariableBinding) {
				IVariableBinding var = (IVariableBinding)name.resolveBinding();
				if (var.isField()) {
					return getPatternStringForField(var);
				}
				else if (var.isParameter()) {
					// TODO
					return var.getName();
				}
				else {
					return getPatternStringForVariable(var);
				}
			} else {
				return "";
			}
		}
		else {
			return "";
		}
	}
	
	/**
	 * 
	 * @param inv
	 * @return sample: qualified.class.name(_,_,"string literal")
	 */
	private static String getPatternStringForInvocation(MethodInvocation inv) {
		StringBuilder result = new StringBuilder();
		Expression exp = inv.getExpression();
		if (exp != null) {
			result.append(exp.resolveTypeBinding().getQualifiedName()); 
		}
		else {
			result.append(ASTUtil.getContainingTypeDeclaration(inv).resolveBinding().getQualifiedName()); 
		}
		
		result.append("." + inv.getName());
		result.append("(");
		for (int i=0; i < inv.arguments().size(); i++) {
			if (i > 0) {
				result.append(",");
			}
			Expression arg = (Expression)inv.arguments().get(i); 
			if (arg instanceof StringLiteral) {
				result.append(((StringLiteral) arg).getEscapedValue());
			}
			else {
				result.append("_");
			}
		}
		
		return result.toString();
	}
	
	private static String getPatternStringForField(IVariableBinding field) {
		// TODO
		return field.getName();
	}
	
	private static String getPatternStringForVariable(IVariableBinding var) {
		// 	TODO try to track down assignments
		//	NameUsage usage = VariableTracker.getLastMod((Name)node);
		return var.getName();
	}
	
}
