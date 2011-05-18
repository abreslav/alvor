package com.googlecode.alvor.gui;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import com.googlecode.alvor.builder.AlvorBuilder;

/**
 * Is used in plugin.xml for showing/hiding menu items
 * @author Aivar
 *
 */
public class AlvorPropertyTester extends PropertyTester {

	/** 
	 * Tests whether receiver (ie. selection) corresponds to single Java project.
	 * If expectedValue has certain content then also checks whether the project has
	 * Alvor builder enabled or not
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.equals("isSingleJavaProject")) {
			Object element = receiver;
			
			// should work both when receiver is list or smth else
			if (receiver instanceof List) {
				if (((List) receiver).size() == 1) {
					element = ((List)receiver).get(0);
				}
				else {
					return false;
				}
			}
			
			if (!(element instanceof IAdaptable)) {
				return false;
			}
			IProject project = (IProject)((IAdaptable) element).getAdapter(IProject.class);
			
			if (project == null || !project.isOpen()) {
				return false;
			}
			
			if (expectedValue != null && expectedValue.equals("hasAlvorBuilder")) {
				return AlvorBuilder.projectHasAlvorBuilderEnabled(project);
			}
			else if (expectedValue != null && expectedValue.equals("doesntHaveAlvorBuilder")) {
				return ! AlvorBuilder.projectHasAlvorBuilderEnabled(project);
			}
			else {
				try {
					return project.getNature("org.eclipse.jdt.core.javanature") != null;
				} catch (CoreException e) {
					return false;
				}
			}
			
		}
		else {
			return false;
		}
		
		
	}

}
