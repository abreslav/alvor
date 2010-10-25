package com.zeroturnaround.alvor.javaproject.builder;


import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class GeneratePropertiesFile implements IObjectActionDelegate {
	private ISelection selection;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
//		System.err.println("DEBUG: selectionChanged");
		this.selection = selection;
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
					.hasNext();) {
				Object element = it.next();
				IProject project = getProject(element);
				
				// We can enable this for any project?
				action.setEnabled(project != null);
			}
		}
	}
	
	@Override
	public void run(IAction action) {
//		System.err.println("DEBUG: run");
		if (selection instanceof IStructuredSelection) {
//			System.err.println("DEBUG: is structuredselection");
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
			.hasNext();) {
//				System.err.println("DEBUG: for");
				Object element = it.next();
				IProject project = getProject(element);
				if (project != null) {
//					System.err.println("DEBUG: project isn't null");
					generatePropertiesFile(project);
				}
			}
		}
	}

	private IProject getProject(Object element) {
		IProject project = null;
		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof IAdaptable) {
			project = (IProject) ((IAdaptable) element)
			.getAdapter(IProject.class);
		}
		return project;
	}

	private void generatePropertiesFile(IProject project) {
//		System.err.println("DEBUG: generatePropertiesFile");
		InputStream in = this.getClass().getResourceAsStream("/resources/sqlchecker-sample.properties");		

		IFile outfile = project.getFile("sqlchecker.properties");

		if (! (outfile.exists() || in == null) ) {
//			System.err.println("DEBUG: outfile doesn't exist and inputstream isn't null");

			try {
				outfile.create(in, 0, null);
//				System.err.println("DEBUG: outfile successfully created");

				in.close();
			} catch (CoreException e) {
//				System.err.println("DEBUG: coreexception");

				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		else
//			System.err.println("DEBUG: outfile.exists(): " + (outfile.exists() ? "true" : "false") + " in == null: " + (in == null ? "true" : "false") );
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}
}