package ee.stacc.productivity.edsl.gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
//import org.eclipse.jface.viewers.CheckStateChangedEvent;
//import org.eclipse.jface.viewers.CheckboxTreeViewer;
//import org.eclipse.jface.viewers.ICheckStateListener;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.custom.BusyIndicator;
//import org.eclipse.swt.events.ModifyEvent;
//import org.eclipse.swt.events.ModifyListener;
//import org.eclipse.swt.layout.GridData;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
//import org.eclipse.ui.forms.events.ExpansionAdapter;
//import org.eclipse.ui.forms.events.ExpansionEvent;
//import org.eclipse.ui.model.WorkbenchLabelProvider;

import ee.stacc.productivity.edsl.main.OptionLoader;


public class AlvorPropertiesEditor extends FormEditor {
//	private boolean isDirty = false;
	private Map<String, Object> props = null;
	
	private Map<String, Object> loadPropertiesFromEditorInput() {
		try {
			// TODO We should use something like this to init this page?
			//			public void init(IEditorSite site, IEditorInput editorInput)
			//				throws PartInitException {
			IEditorInput editorInput = getEditorInput();
			if (!(editorInput instanceof IFileEditorInput))
				throw new PartInitException("Invalid Input: Must be IFileEditorInput");
				
			java.io.File propFile = ((IFileEditorInput) editorInput).getFile().getLocation().toFile();	
			props = OptionLoader.getFileSqlCheckerProperties(propFile);
			
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				e.getStatus());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return props;
	}
	
	@Override
	protected void addPages() {
		try {
			addPage(new AlvorPropertiesPage(this));
		} catch  (PartInitException e) {
			//			ErrorDialog.openError(
			//					getSite().getShell(),
			//					"Error creating nested text editor",
			//					null,
			//					e.getStatus());
		}

		// TODO: May put this editor back if it makes sense later...
		//			editor = new TextEditor();
		//			int index = addPage(editor, getEditorInput());
		//			setPageText(index, editor.getTitle());
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		commitPages(true);
		editorDirtyStateChanged();
		getEditor(1).doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(1, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

//	private void setDirty(boolean dirty) {
//		this.isDirty = dirty;
//		firePropertyChange(PROP_DIRTY);
//	}

//	public boolean isDirty() {
//		return this.isDirty;
//	}
}



