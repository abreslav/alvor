package com.zeroturnaround.alvor.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
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
	
	public static void showDialog(final String msg, final String caption) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				if (caption == "Warning") {
					MessageDialog.openWarning(shell, caption, msg);
				}
				else if (caption == "Problem" || caption == "Error") {
					MessageDialog.openError(shell, caption, msg);
				}
				else {
					MessageDialog.openInformation(shell, caption, msg);
				}
			}
		});
	}
	
	public static void showErrorDialog(final String msg, Throwable error) {
		final IStatus status = new Status(IStatus.ERROR, "com.zeroturnaround.alvor.gui", 1, "Error", error);
		
		IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (win != null) {
			ErrorDialog.openError(win.getShell(), "Info", msg, status);
		}
		else { // don't know why is null, but ...
				// http://dev.eclipse.org/newslists/news.eclipse.platform/msg68395.html
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getShell(), 
							"Info", msg, status);
				}
			});
		}
	}
	
	public static IProject getCurrentProject() {
		IEditorPart editor = 
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IEditorInput input = editor.getEditorInput();
		
		IFile file = null;
		if (input instanceof IFileEditorInput) {
			file = ((IFileEditorInput)input).getFile();
		}
		if (file==null) {
			return null;
		} else {
			IProject project = file.getProject();
			return project;
		}
	}
	
	public static IJavaProject getCurrentJavaProject() {
		return JavaCore.create(getCurrentProject());
	}
	
	public static void setStatusbarMessage(final String message) {
		// http://robertvarttinen.blogspot.com/2007/02/writing-to-statusbar-in-eclipse-from.html
		final Display display = Display.getDefault();

		new Thread() {

			public void run() {

				display.syncExec(new Runnable() {
					/*
					 * (non-Javadoc)
					 *
					 * @see java.lang.Runnable#run()
					 */
					public void run() {

						IWorkbench wb = PlatformUI.getWorkbench();
						IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

						IWorkbenchPage page = win.getActivePage();

						IWorkbenchPart part = page.getActivePart();
						IWorkbenchPartSite site = part.getSite();

						IViewSite vSite = ( IViewSite ) site;

						IActionBars actionBars =  vSite.getActionBars();

						if( actionBars == null )
							return ;

						IStatusLineManager statusLineManager =
							actionBars.getStatusLineManager();

						if( statusLineManager == null )
							return ;

						statusLineManager.setMessage( message );
					}
				});
			}
		}.start();		
	}
}
