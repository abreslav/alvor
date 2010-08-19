package ee.stacc.productivity.edsl.gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ee.stacc.productivity.edsl.main.OptionLoader;

public class AlvorPropertiesEditor extends FormEditor {
	private TextEditor editor;

	private class AlvorPropertiesPage extends FormPage {
		public static final String PAGE_ID = "properties"; //$NON-NLS-1$

		public AlvorPropertiesPage(FormEditor editor) {
			super(editor, PAGE_ID, "Alvor configuration");
		}

		protected void createFormContent(IManagedForm managedForm) {
			FormToolkit toolkit = managedForm.getToolkit();
			Composite composite = managedForm.getForm().getBody();
			TableWrapLayout layout = new TableWrapLayout();
			composite.setLayout(layout);
			
			ScrolledForm form = toolkit.createScrolledForm(composite);
			layout = new TableWrapLayout();
			layout.numColumns = 2;
			form.getBody().setLayout(layout);

			TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
			Label label = toolkit.createLabel(form.getBody(), "foobar"); //$NON-NLS-1$
			td.colspan = 2;
			label.setLayoutData(td);

			Map<String, Object> props = loadPropertiesFromEditorInput();

			for (Map.Entry<String, Object> entry : props.entrySet()) {
				td = new TableWrapData(TableWrapData.FILL_GRAB);
				label = toolkit.createLabel(form.getBody(), entry.getKey() +":"); //$NON-NLS-1$
				td = new TableWrapData(TableWrapData.RIGHT);
				label.setLayoutData(td);
				td = new TableWrapData(TableWrapData.FILL_GRAB);
				Text text = toolkit.createText(form.getBody(), entry.getValue().toString()); //$NON-NLS-1$
				text.setLayoutData(td);
			}
		}
	}

	private Map<String, Object> loadPropertiesFromEditorInput() {
		Map<String, Object> props = null;
		
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
			editor = new TextEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch  (PartInitException e) {
//			ErrorDialog.openError(
//					getSite().getShell(),
//					"Error creating nested text editor",
//					null,
//					e.getStatus());
		}
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
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

}


