package ee.stacc.productivity.edsl.gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
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
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ee.stacc.productivity.edsl.main.OptionLoader;

import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.core.build.IBuildModel;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ui.editor.build.BuildInputContext;
import org.eclipse.pde.internal.ui.editor.build.BuildContentsSection.TreeContentProvider;
import org.eclipse.pde.internal.ui.editor.context.InputContext;

public class AlvorPropertiesEditor extends FormEditor {
//	private boolean isDirty = false;
	private Map<String, Object> props = null;
	
	
	private class AlvorPropertiesSection extends SectionPart implements IModelChangedListener {
		
		AlvorPropertiesSection(Composite parent,
				FormToolkit toolkit,
				int style) {
			super(parent, toolkit, style);
			getSection().setText("Section title");
			getSection().setDescription("This is the description that goes below the title");

//			getBuildModel().addModelChangedListener(this);
			createClient(getSection(), toolkit);
		}
	
//		This has to be implemented without InputContext
//		private IBuildModel getBuildModel() {
//			InputContext context = getPage().getPDEEditor().getContextManager().findContext(BuildInputContext.CONTEXT_ID);
//			if (context == null)
//				return null;
//			return (IBuildModel) context.getModel();
//		}

		public void createClient(final Section section, FormToolkit toolkit) {
			Composite container = toolkit.createComposite(section);
			container.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 2));
			
			Composite sectionClient = toolkit.createComposite(getSection());
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 2;
			sectionClient.setLayout(layout);

			//				Composite container = createClientContainer(section, 2, toolkit);
			//				fBuildModel = getBuildModel();
			//				if (fBuildModel.getUnderlyingResource() != null)
			//					fBundleRoot = PDEProject.getBundleRoot(fBuildModel.getUnderlyingResource().getProject());
			//
			//				fTreeViewer = new CheckboxTreeViewer(toolkit.createTree(container, SWT.CHECK));
			//				fTreeViewer.setContentProvider(new TreeContentProvider());
			//				fTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
			//				fTreeViewer.setAutoExpandLevel(0);
			//				fTreeViewer.addCheckStateListener(new ICheckStateListener() {
			//
			//					public void checkStateChanged(final CheckStateChangedEvent event) {
			//						final Object element = event.getElement();
			//						BusyIndicator.showWhile(section.getDisplay(), new Runnable() {
			//
			//							public void run() {
			//								if (element instanceof IFile) {
			//									IFile file = (IFile) event.getElement();
			//									handleCheckStateChanged(file, event.getChecked());
			//								} else if (element instanceof IFolder) {
			//									IFolder folder = (IFolder) event.getElement();
			//									handleCheckStateChanged(folder, event.getChecked());
			//								}
			//							}
			//						});
			//					}
			//				});
			//				GridData gd = new GridData(GridData.FILL_BOTH);
			//				gd.heightHint = 100;
			//				gd.widthHint = 100;
			//				fTreeViewer.getTree().setLayoutData(gd);
			//				initialize();
			//				toolkit.paintBordersFor(container);
			//				createViewerPartControl(container, SWT.FULL_SELECTION, 2, toolkit);
			//				section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
			//				section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			//				section.setClient(container);



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

		@Override
		public void modelChanged(IModelChangedEvent event) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	
	
	private class AlvorPropertiesPage extends FormPage {
		public static final String PAGE_ID = "properties"; //$NON-NLS-1$
//		private BuildClasspathSection fClasspathSection;
//		private BuildContentsSection fSrcSection;
//		private BuildContentsSection fBinSection;
//		private RuntimeInfoSection fRuntimeSection;
		
		public AlvorPropertiesPage(FormEditor editor) {
			super(editor, PAGE_ID, "Alvor configuration");
		}

		protected void createFormContent(IManagedForm managedForm) {
			super.createFormContent(managedForm);

//			FormToolkit toolkit = mform.getToolkit();
//			ScrolledForm form = mform.getForm();

//			form.setImage(PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_BUILD_EXEC));
//			form.setText(PDEUIMessages.BuildEditor_BuildPage_title);
//			form.getBody().setLayout(FormLayoutFactory.createFormGridLayout(true, 2));

//			Composite composite = managedForm.getForm().getBody();
//			composite.setLayout(new TableWrapLayout());

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
			

//			fRuntimeSection = new RuntimeInfoSection(this, form.getBody());
//
//			fBinSection = new BinSection(this, form.getBody());
//			fBinSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
//
//			fSrcSection = new SrcSection(this, form.getBody());
//			fSrcSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
//
//			fClasspathSection = new BuildClasspathSection(this, form.getBody());
//
//			mform.addPart(fRuntimeSection);
//			mform.addPart(fSrcSection);
//			mform.addPart(fBinSection);
//			mform.addPart(fClasspathSection);
			
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



