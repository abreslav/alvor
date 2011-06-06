package com.googlecode.alvor.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class CheckersManager {
	private static final String CHECKERS_ID = "com.googlecode.alvor.checkers.checkers";
	public static IAbstractStringChecker getCheckerByName(String name) {
		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(CHECKERS_ID);
			for (IConfigurationElement e : config) {
				if (e.getAttribute("name").equals(name)) {
					return (IAbstractStringChecker)e.createExecutableExtension("class");
				}
			}
			
			throw new IllegalArgumentException("Found no checker with name=" + name);
		} catch (CoreException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static List<CheckerInfo> getAvailableCheckersInfo() {
		List<CheckerInfo> result = new ArrayList<CheckerInfo>();
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(CHECKERS_ID);
		for (IConfigurationElement e : config) {
			result.add(new CheckerInfo(e.getAttribute("name"), e.getAttribute("description"), 
					"true".equals(e.getAttribute("usesDatabase")), e.getAttribute("defaultDriver")));
		}

		return result;
	}

}
