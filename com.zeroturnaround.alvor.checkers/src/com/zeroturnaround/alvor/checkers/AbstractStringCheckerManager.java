package com.zeroturnaround.alvor.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class AbstractStringCheckerManager {

	public static final AbstractStringCheckerManager INSTANCE = new AbstractStringCheckerManager();
	private static final String CHECKERS_ID = "com.zeroturnaround.alvor.checkers.checkers";
	private static final ILog LOG = Logs.getLog(AbstractStringCheckerManager.class); 
	
	private List<IAbstractStringChecker> checkers = null;
	
	private AbstractStringCheckerManager() {}
	
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
						assert LOG.message(o);
					}
				}
			} catch (CoreException e) {
				LOG.exception(e);
				throw new IllegalArgumentException(e);
			}
		}
		return checkers;
	}
}
