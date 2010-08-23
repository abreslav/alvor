package ee.stacc.productivity.edsl.gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.forms.SectionPart;
import ee.stacc.productivity.edsl.main.OptionLoader;

public class AlvorPropertiesEditor extends FormEditor {
//	private boolean isDirty = false;
	private Map<String, Object> props = null;

	private class AlvorPropertiesSection extends SectionPart {
		AlvorPropertiesSection(Composite parent,
				FormToolkit toolkit,
				int style) {
			super(parent, toolkit, style);
			Section section = getSection();
			
			section.setText("Section title");
			section.setDescription("This is the description that goes "+
			"below the title");

			Composite sectionClient = toolkit.createComposite(section);
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 2;
			sectionClient.setLayout(layout);

			Label label = toolkit.createLabel(sectionClient, "barbar"); //$NON-NLS-1$
			TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
			td.colspan = 2;
			label.setLayoutData(td);

			Map<String, Object> props = loadPropertiesFromEditorInput();

			for (Map.Entry<String, Object> entry : props.entrySet()) {
				td = new TableWrapData(TableWrapData.FILL_GRAB);
				label = toolkit.createLabel(sectionClient, entry.getKey() +":"); //$NON-NLS-1$
				td = new TableWrapData(TableWrapData.RIGHT);
				label.setLayoutData(td);
				td = new TableWrapData(TableWrapData.FILL_GRAB);
				Text text = toolkit.createText(sectionClient, entry.getValue().toString()); //$NON-NLS-1$
				text.setLayoutData(td);
//				text.addModifyListener(new ModifyListener() {
//					public void modifyText(ModifyEvent e) {
//						setDirty(true);
//					}
//				});
			}
		}
	}
	
	private class AlvorPropertiesPage extends FormPage {
		public static final String PAGE_ID = "properties"; //$NON-NLS-1$

		public AlvorPropertiesPage(FormEditor editor) {
			super(editor, PAGE_ID, "Alvor configuration");
		}

		protected void createFormContent(IManagedForm managedForm) {
			FormToolkit toolkit = managedForm.getToolkit();
			Composite composite = managedForm.getForm().getBody();
			composite.setLayout(new TableWrapLayout());

			Label label = toolkit.createLabel(composite, "foobar"); //$NON-NLS-1$
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			
			AlvorPropertiesSection section = new AlvorPropertiesSection(composite, toolkit,
					Section.DESCRIPTION|Section.TITLE_BAR|Section.TWISTIE|Section.EXPANDED);
			section.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			
//			section.addExpansionListener(new ExpansionAdapter() {
				//				public void expansionStateChanged(ExpansionEvent e) {
				//					this.managedForm.reflow(true);
				//				}
				//			});
				
			managedForm.addPart(section);
			
			//TODO Later
			//// assuming 'editorPart' is an instance of an org.eclipse.ui.IEditorPart
//			ITextEditor editor = (ITextEditor) editorPart.getAdapter(ITextEditor.class):
//			if (editor != null) {
//			  IDocumentProvider provider = editor.getDocumentProvider();
//			  IDocument document = provider.getDocument(editor.getEditorInput());
//			}
		}
	}

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
//			editor = new TextEditor();
//			int index = addPage(editor, getEditorInput());
//			setPageText(index, editor.getTitle());
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

	private void setDirty(boolean dirty) {
//		this.isDirty = dirty;
		firePropertyChange(PROP_DIRTY);
	}

//	public boolean isDirty() {
//		return this.isDirty;
//	}
}



