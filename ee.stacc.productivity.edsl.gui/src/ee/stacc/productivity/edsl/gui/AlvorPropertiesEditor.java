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
//import org.eclipse.ui.forms.events.ExpansionAdapter;
//import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.forms.SectionPart;
//import org.eclipse.ui.model.WorkbenchLabelProvider;

import ee.stacc.productivity.edsl.main.OptionLoader;

import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
//import org.eclipse.pde.core.build.IBuildModel;

public class AlvorPropertiesEditor extends FormEditor {
//	private boolean isDirty = false;
	private Map<String, Object> props = null;
	
	
	private class AlvorPropertiesSection extends SectionPart implements IModelChangedListener {
		private FormPage page;
		
		AlvorPropertiesSection(FormPage page, Composite parent) {
			super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TWISTIE|Section.EXPANDED);
			this.page = page;
			
			getSection().setText("Section title");
			getSection().setDescription("This is the description that goes below the title");

//			getBuildModel().addModelChangedListener(this);
			createClient(getSection(), page.getManagedForm().getToolkit());
		}
	
//		,
//		This has to be implemented without InputContext
//		private IBuildModel getBuildModel() {
//			InputContext context = getPage().getPDEEditor().getContextManager().findContext(BuildInputContext.CONTEXT_ID);
//			if (context == null)
//				return null;
//			return (IBuildModel) context.getModel();
//		}

		public void createClient(final Section section, FormToolkit toolkit) {
			Composite container = toolkit.createComposite(section);
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 2;
			container.setLayout(layout);
			
			//				fBuildModel = getBuildModel();

			Label label = toolkit.createLabel(container, "barbar"); //$NON-NLS-1$
			TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
			td.colspan = 2;
			label.setLayoutData(td);

			label = toolkit.createLabel(container, "epan:"); //$NON-NLS-1$
			td = new TableWrapData(TableWrapData.FILL_GRAB|TableWrapData.RIGHT);
			label.setLayoutData(td);

			Text text = toolkit.createText(container, "bepan"); //$NON-NLS-1$
			td = new TableWrapData(TableWrapData.FILL_GRAB);
			text.setLayoutData(td);			
			
//			Map<String, Object> props = loadPropertiesFromEditorInput();
//
//			for (Map.Entry<String, Object> entry : props.entrySet()) {
//				label = toolkit.createLabel(sectionClient, entry.getKey() +":"); //$NON-NLS-1$
//				td = new TableWrapData(TableWrapData.FILL_GRAB|TableWrapData.RIGHT);
//				label.setLayoutData(td);
//				Text text = toolkit.createText(sectionClient, entry.getValue().toString()); //$NON-NLS-1$
//				td = new TableWrapData(TableWrapData.FILL_GRAB);
//				text.setLayoutData(td);
//			/*	text.addModifyListener(new ModifyListener() {
//					public void modifyText(ModifyEvent e) {
//						setDirty(true);
//					}
//				});*/
//			}

			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			section.setClient(container);
		}

		@Override
		public void modelChanged(IModelChangedEvent event) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	
	
	private class AlvorPropertiesPage extends FormPage {
		public static final String PAGE_ID = "properties"; //$NON-NLS-1$
		private AlvorPropertiesEditor.AlvorPropertiesSection section;

		public AlvorPropertiesPage(FormEditor editor) {
			super(editor, PAGE_ID, "Alvor configuration");
		}

		protected void createFormContent(IManagedForm mform) {
			super.createFormContent(mform);
			FormToolkit toolkit = mform.getToolkit();
			ScrolledForm form = mform.getForm();
			form.getBody().setLayout(new TableWrapLayout());	
//			form.setText(PDEUIMessages.BuildEditor_BuildPage_title);

			Label label = toolkit.createLabel(form.getBody(), "foobar"); //$NON-NLS-1$
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
						
			section = new AlvorPropertiesEditor.AlvorPropertiesSection(this, form.getBody());
			
			// TODO: We want to keep it expanded all the time...
			//			section.addExpansionListener(new ExpansionAdapter() {
			//				public void expansionStateChanged(ExpansionEvent e) {
			//					this.managedForm.reflow(true);
			//				}
			//			});		

			mform.addPart(section);
			
			// TODO IDocument management later
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



