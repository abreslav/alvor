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
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
			.hasNext();) {
				Object element = it.next();
				IProject project = getProject(element);
				if (project != null) {
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
		InputStream in = this.getClass().getResourceAsStream("/resources/sqlchecker-sample.properties");		

		IFile outfile = project.getFile("sqlchecker.properties");

		if (! (outfile.exists() || in == null) ) {
			try {
				outfile.create(in, 0, null);
				in.close();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		//
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}
}