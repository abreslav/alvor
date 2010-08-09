package com.zeroturnaround.alvor.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

public class GuiUtil {
	public static List<IJavaElement> getSelectedJavaElements() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		List<IJavaElement> result = new ArrayList<IJavaElement>();
		
		if (selection instanceof StructuredSelection) {
			StructuredSelection structSel = (StructuredSelection) selection;
			for (Object element : structSel.toList()) {
				if (element instanceof IJavaElement) {
					result.add((IJavaElement)element);
				}
			}
			return result;
		}
		throw new IllegalStateException("No Java element selected");
	}

	// Adapted from the Eclipse FAQ - CJ
	public static IJavaProject getSelectedJavaProject() {
		IProject project;
		
		IJavaElement element = getSelectedJavaElements().get(0);
		
		if (element instanceof IResource) {
			project = ((IResource) element).getProject();
		}
		else if (!(element instanceof IAdaptable)) {
			return null;
		}
		else {
			IAdaptable adaptable = (IAdaptable)element;
			Object adapter = adaptable.getAdapter(IResource.class);
			project = ((IResource) adapter).getProject();
		}		
		
		return JavaCore.create(project);
	}
}
