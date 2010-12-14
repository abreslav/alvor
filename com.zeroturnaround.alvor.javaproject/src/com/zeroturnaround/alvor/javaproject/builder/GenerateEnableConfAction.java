package com.zeroturnaround.alvor.javaproject.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class GenerateEnableConfAction implements IObjectActionDelegate {
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
			// Iterating through any selected elements
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
			.hasNext();) {
//				System.err.println("DEBUG: for");
				Object element = it.next();
				IProject project = getProject(element);
				// If this element had a project
				if (project != null) {
//					System.err.println("DEBUG: project isn't null");
					generatePropertiesFile(project);
					addNature(project);
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
				e.printStackTrace();
			}
		}
	}

	/**
	 * Toggles sample nature on a project
	 * 
	 * @param project
	 *            to have sample nature added or removed
	 */
	private void addNature(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			// This is just an ugly way to check it's not there yet...
			for (int i = 0; i < natures.length; ++i) {
				if (ESQLNature.NATURE_ID.equals(natures[i])) {
					return;
				}
			}

			// Add the nature 
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = ESQLNature.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/* 
	 // Should move this sometime
	private void removeNature(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (ESQLNature.NATURE_ID.equals(natures[i])) {
					// Remove the nature
					String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i,
							natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);
					return;
				}
			}
		} catch (CoreException e) {
		}
	}
 */
	
	
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}
}
