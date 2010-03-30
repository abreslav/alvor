package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;

public class NameAssignment extends NameUsage {
	private Assignment.Operator operator;
	private Expression expr;
	
	public NameAssignment(Assignment.Operator operator, Expression expr) {
		this.operator = operator;
		this.expr = expr;
	}
}
