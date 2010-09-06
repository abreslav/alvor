package com.zeroturnaround.alvor.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.ErrorDialog;
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

	public static IJavaElement getSingleSelectedJavaElement() throws IllegalAccessException {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection structSel = (StructuredSelection) selection;
			if (structSel.size() != 1) {
				throw new IllegalAccessException("One element should be selected");
			}
			else {
				Object sel = structSel.iterator().next();
				if (sel instanceof IJavaElement) {
					return (IJavaElement)sel;
				}
				else {
					throw new IllegalAccessException("Expected a Java element");
				}
			}
		}
		throw new IllegalAccessException("Can't find selected Java element");
	}
	
	public static void ShowErrorDialog(String msg) {
		IStatus status = new Status(IStatus.ERROR, 
				"com.zeroturnaround.alvor.gui", 1, "Error", null);
		ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
			      "This is your final warning", null, status);
	}
}
