package ee.stacc.productivity.edsl.crawler;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Statement;

public class VariableTracker {
	public static List<ASTNode> getPreviousAssignments
		(IVariableBinding var, Statement currentStmt) {
		return null;
	}
}
