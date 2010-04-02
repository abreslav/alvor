package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class NameInArgument extends NameUsage {
	MethodInvocation inv;
	
	public MethodInvocation getInv() {
		return inv;
	}
}
