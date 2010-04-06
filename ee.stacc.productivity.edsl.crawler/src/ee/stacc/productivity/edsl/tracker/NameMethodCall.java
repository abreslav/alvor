package ee.stacc.productivity.edsl.tracker;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

public class NameMethodCall extends NameUsage {
	private MethodInvocation inv;
	private Name object;
	
	public NameMethodCall(MethodInvocation inv, Name object) {
		this.inv = inv;
		this.object = object;
	}

	public MethodInvocation getInv() {
		return inv;
	}
	
	public Name getObject() {
		return object;
	}

}
