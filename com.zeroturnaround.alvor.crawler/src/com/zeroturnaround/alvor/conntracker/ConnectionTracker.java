package com.zeroturnaround.alvor.conntracker;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;


public class ConnectionTracker {
	public static String getConnectionDescriptor(Expression node) {
		if (node == null) {
			System.err.println("CONN NULL");
		}
		else if (node instanceof MethodInvocation) {
			// create string describing this method invocation
			System.err.println("CONN INV: " + node);
		}
		else if (node instanceof Name) {
			Name name = (Name)node;
			IVariableBinding var = (IVariableBinding)name.resolveBinding();
			if (var.isField()) {
				System.err.println("CONN FIELD: " + var);
			}
			else if (var.isParameter()) {
				System.err.println("CONN PARAM: " + var);
			}
			else {
				return "?"; // will be checked against all databases
				// TODO try to track down assignments
				//NameUsage usage = VariableTracker.getLastMod((Name)node);
			}
		}
		
		return "*";
	}
	
}
