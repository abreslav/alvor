package ee.stacc.productivity.edsl.conntracker;

import java.io.ObjectInputStream.GetField;
import java.rmi.activation.UnknownObjectException;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import ee.stacc.productivity.edsl.crawler.NodeSearchEngine;
import ee.stacc.productivity.edsl.crawler.PositionUtil;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.tracker.NameAssignment;
import ee.stacc.productivity.edsl.tracker.NameInParameter;
import ee.stacc.productivity.edsl.tracker.NameUsage;
import ee.stacc.productivity.edsl.tracker.VariableTracker;

public class ConnectionTracker {
	public static ConnectionDescriptor getConnectionDescriptorForHotspot(IPosition pos) {
		Expression arg = (Expression) NodeSearchEngine.getASTNode(pos);
		assert(arg != null);
		MethodInvocation inv = (MethodInvocation)arg.getParent();
		assert(inv != null);
		assert(inv.getExpression() != null);
		return getConnectionDescriptor(inv.getExpression());
	}
	
	public static ConnectionDescriptor getConnectionDescriptor(Expression node) {
		try {
			return getConnectionDesc(node);
		} 
		catch (UnsupportedOperationException e) {
			return new ConnectionDescriptor(PositionUtil.getPosition(node)
					, "UNSUPPORTED: " + e.getMessage());
		}
		catch (Throwable e) {
			return new ConnectionDescriptor(PositionUtil.getPosition(node)
					, "ERROR: " + e.getMessage());
		}
	}
	
	private static ConnectionDescriptor getConnectionDesc(Expression expr) {
		if (! isConnectionOrDataSource(expr.resolveTypeBinding())) {
			IPosition pos = PositionUtil.getPosition(expr);
			int lineNo = PositionUtil.getLineNumber(pos);
			throw new UnsupportedOperationException("getConnectionDesc, typeBinding="
					+ expr.resolveTypeBinding().getName());
		}
		if (expr instanceof Name) {
			return getConnectionDescForName((Name)expr);
		}
		else if (expr instanceof MethodInvocation) {
			MethodInvocation inv = (MethodInvocation) expr;
			if (inv.getExpression() != null
					&& isDataSource(inv.getExpression().resolveTypeBinding())
					&& inv.getName().getIdentifier().equals("getConnection")) {
				return getConnectionDesc(inv.getExpression());
			}
			else {
				return new ConnectionDescriptor(PositionUtil.getPosition(inv),
						inv.toString());
			}
		}
		else {
			throw new UnsupportedOperationException("getConnectionSource(" + expr.getClass() + ")");
		}
	}
	
	private static ConnectionDescriptor getConnectionDescForName(Name name) {
		if (((IVariableBinding)name.resolveBinding()).isField()) {
			throw new UnsupportedOperationException("Fields not supported");
		}
		
		NameUsage usage = VariableTracker.getLastMod(name);
		
		if (usage instanceof NameAssignment) {
			assert ((NameAssignment) usage).getOperator().equals(Assignment.Operator.ASSIGN);
			return getConnectionDesc(((NameAssignment) usage).getRightHandSide());
		}
		else if (usage instanceof NameInParameter) {
			// find all callsites ...
			throw new UnsupportedOperationException("getLastMod(conn) => " + usage.getClass());
		}
		else {
			throw new UnsupportedOperationException("getLastMod(conn) => " + usage.getClass());
		}
	}
	
	private static boolean isConnectionOrDataSource(ITypeBinding typeBinding) {
		return isConnection(typeBinding) || isDataSource(typeBinding);
	}
	
	private static boolean isConnection(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("java.sql.Connection");
	}
	
	private static boolean isDataSource(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("javax.sql.DataSource");
	}
	
	private static boolean isDriverManager(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("java.sql.DriverManager");
	}
	
	private static boolean isSourceCall(MethodInvocation inv) {
		ITypeBinding type = inv.resolveTypeBinding();
		return inv.getName().getIdentifier().equals("getConnection");
	}
	
}
