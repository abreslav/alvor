package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;

public class NameAssignment extends NameUsage {
	private Assignment.Operator operator;
	private Expression valueExpression;
	
	public NameAssignment(Assignment.Operator operator, Expression expr) {
		this.operator = operator;
		this.valueExpression = expr;
	}
	
	public Expression getValueExpression() {
		return valueExpression;
	}
}
