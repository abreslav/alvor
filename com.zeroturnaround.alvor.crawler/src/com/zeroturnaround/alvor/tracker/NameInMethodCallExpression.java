package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class NameInMethodCallExpression extends NameUsage {
	private MethodInvocation inv;
	private Expression expression;
	
	public NameInMethodCallExpression(MethodInvocation inv, Expression expression) {
		this.inv = inv;
		this.expression = expression;
	}

	public MethodInvocation getInv() {
		return inv;
	}
	
	public ASTNode getNode() {
		return expression;
	}
	
}
