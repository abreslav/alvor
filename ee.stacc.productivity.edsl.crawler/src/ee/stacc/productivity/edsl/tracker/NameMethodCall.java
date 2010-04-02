package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class NameMethodCall extends NameUsage {
	MethodInvocation inv;
	
	public MethodInvocation getInv() {
		return inv;
	}

}
