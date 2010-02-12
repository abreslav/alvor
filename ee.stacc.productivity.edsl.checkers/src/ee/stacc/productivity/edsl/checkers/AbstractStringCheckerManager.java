package ee.stacc.productivity.edsl.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class AbstractStringCheckerManager {

	public static final AbstractStringCheckerManager INSTANCE = new AbstractStringCheckerManager();
	private static final String CHECKERS_ID = "ee.stacc.productivity.edsl.checkers.checkers";
	
	private List<IAbstractStringChecker> checkers = null;
	
	public List<IAbstractStringChecker> getCheckers() {
		if (checkers == null) {
			checkers = new ArrayList<IAbstractStringChecker>();			
			try {
				IConfigurationElement[] config = Platform.getExtensionRegistry()
						.getConfigurationElementsFor(CHECKERS_ID);
				for (IConfigurationElement e : config) {
					final Object o = e.createExecutableExtension("class");
					if (o instanceof IAbstractStringChecker) {
						checkers.add((IAbstractStringChecker) o);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(e);
			}
		}
		return checkers;
	}
}
