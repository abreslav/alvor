package ee.stacc.productivity.edsl.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import ee.stacc.productivity.edsl.main.OptionLoader;

/*
 * These are the component of current sqlchecker.properties to deal with:
 * 
 * In future, we may have multiple instances of DB*:
 * DBDriverName=oracle.jdbc.OracleDriver
 * DBDriverName=org.hsqldb.jdbc.JDBCDriver
 *     - this is a class which can be selected from somewhere... ?
 * DBUrl=jdbc:oracle:thin:@localhost:1521:xe
 * DBUrl=jdbc:hsqldb:file:/Users/cj/Documents/Workspaces/EmbSQL-tests/SampleProject/db/sample_db;shutdown=true;ifexists=true
 * - what is a reasonable way to build a DBUrl, or should it just be typed in... ?
 * DBUsername=compiere
 * DBUsername=sa
 * DBPassword=password
 * DBPassword=
 * - user/password may be empty, password should be *'ed? 
 *
 * hotspots=java.sql.Connection,prepareStatement,1
 * hotspots=java.sql.Connection,prepareStatement,1;\
 * java.sql.Connection,prepareCall,1;\
 * java.sql.Statement,execute,1;\
 * java.sql.Statement,executeQuery,1;\
 * java.sql.Statement,executeUpdate,1;\
 * com.missiondata.oss.sqlprocessor.SQLProcessor,new,2;
 * - this is class, method and argument number containing the sql string
 * - will they be common between datasources? Not an immediate concern
*/

public class AlvorPropertiesEditor extends FormEditor {
	//	private boolean isDirty = false;
	private AlvorPropertiesModel model;
	
	public class AlvorPropertiesModel {
		public final String sdbdrivername =	"DBDriverName";
		public final String sdburl = "DBUrl";
		public final String sdbusername = "DBUsername";
		public final String sdbpassword = "DBPassword"; 
		public final String shotspots = "hotspots";
		
/*		public class Hotspot {
			public String pkg;
			public String method;
			public int argnr; // 1-indexed?
			
			Hotspot(String pkg, String method, int argnr) {
				this.pkg = pkg;
				this.method = method;
				this.argnr = argnr;
			}
			
			Hotspot(String commaseparated) {
				// ...
			}
		}
*/		
		private EditorPart editorPart;
		
		private String dbdrivername = null;
		private String dburl = null;
		private String dbusername = null;
//		private List<Hotspot> hotspots = null;
		private String hotspots = null;
		
		public String getDbdrivername() {
			return dbdrivername;
		}
		
		public AlvorPropertiesModel(EditorPart editorPart) {
			// Do we maybe need this for later, signaling or something?
			this.editorPart = editorPart;	
			refresh();
		}

		public void refresh() {
			Properties props = loadProps();
				
			// (getProperty defaults to null)
			dbdrivername = props.getProperty(sdbdrivername);
			dburl = props.getProperty(sdburl);
			dbusername = props.getProperty(sdbusername);
			hotspots = props.getProperty(shotspots);
		}

		private Properties loadProps() {
			ITextEditor editor = (ITextEditor) editorPart.getAdapter(ITextEditor.class);

			IDocumentProvider provider = editor.getDocumentProvider();
			IDocument document = provider.getDocument(editor.getEditorInput());

			Properties props = new Properties();
			try {
				props.load(new ByteArrayInputStream(document.get().getBytes("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return props;
		}
		
		private void saveProps(Properties props) {
			ITextEditor editor = (ITextEditor) editorPart.getAdapter(ITextEditor.class);

			IDocumentProvider provider = editor.getDocumentProvider();
			IDocument document = provider.getDocument(editor.getEditorInput());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				props.store(baos, null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			document.set(baos.toString());
		}
		
		private void setProperty(String key, String value) {
			Properties props = loadProps();
			props.setProperty(key, value);
			saveProps(props);
		}
		
		public String getDburl() {
			return dburl;
		}
		
		public String getDbusername() {
			return dbusername;		
		}
		
//		public List<Hotspot> getHotspots() {
		public String getHotspots() {
			return hotspots;
		}
		
		// TODO: Need to also set document here, which will mark things dirty?
		public void setDbdrivername(String dbdrivername) {
			this.dbdrivername = dbdrivername;
			// TODO: Can also check if they are actually changed...
			setProperty(sdbdrivername, dbdrivername);
		}
		
		public void setDburl(String dburl) {
			this.dburl = dburl;
			setProperty(sdburl, dburl);
		}
		
		public void getDbusername(String dbusername) {
			this.dbusername = dbusername;
			setProperty(sdbusername, dbusername);
		}
		
//		public void setHotspots(List<Hotspot> hotspots) {
		public void setHotspots(String hotspots) {
			this.hotspots = hotspots;
			setProperty(shotspots, hotspots);
		}
	}
	
	
	
	public class AlvorPropertiesSection extends SectionPart {
		private FormPage page;

		AlvorPropertiesSection(FormPage page, Composite parent) {
			super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION|Section.TITLE_BAR|Section.TWISTIE|Section.EXPANDED);
			this.page = page;

			getSection().setText("Section title");
			getSection().setDescription("This is the description that goes below the title");

//			TODO: How would I do this... ?
//			 getPropertiesModel().addModelChangedListener(this);
			createClient(getSection(), page.getManagedForm().getToolkit());
		}

		//		TODO: This has to be implemented without InputContext
		// 
		//		private IBuildModel getBuildModel() {
		//			InputContext context = getPage().getPDEEditor().getContextManager().findContext(BuildInputContext.CONTEXT_ID);
		//			if (context == null)
		//				return null;
		//			return (IBuildModel) context.getModel();
		//		}

		private AlvorPropertiesModel getPropertiesModel() {
			FormEditor editor = getPage().getEditor();
			if (editor instanceof AlvorPropertiesEditor)
				return ((AlvorPropertiesEditor) editor).getModel();
			else
				return null;
		}
		
		private FormPage getPage() {
			return page;
		}
		
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
			label.setLayoutData(new TableWrapData());

			Text text = toolkit.createText(container, "bepan"); //$NON-NLS-1$
			text.setLayoutData(new TableWrapData());			

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



//		public void modelChanged(IModelChangedEvent event) {
//			if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED)
//				markStale();
//			Object changeObject = event.getChangedObjects()[0];
//			String keyName = event.getChangedProperty();
			//
			//			// check if model change applies to this section
			//			if (!(changeObject instanceof IBuildEntry))
			//				return;
			//			IBuildEntry entry = (IBuildEntry) changeObject;
			//			String entryName = entry.getName();
			//			if (!entryName.startsWith(IBuildEntry.JAR_PREFIX) && !entryName.equals(PROPERTY_JAR_ORDER) && !entryName.equals(PROPERTY_BIN_INCLUDES))
			//				return;
			//
			//			if (entryName.equals(PROPERTY_BIN_INCLUDES))
			//				return;
			//
			//			int type = event.getChangeType();
			//
			//			// account for new key
			//			if (entry.getName().startsWith(PROPERTY_SOURCE_PREFIX)) {
			//				IStructuredSelection newSel = null;
			//				if (type == IModelChangedEvent.INSERT) {
			//					fLibraryViewer.add(entry);
			//					newSel = new StructuredSelection(entry);
			//				} else if (type == IModelChangedEvent.REMOVE) {
			//					int index = fLibraryViewer.getTable().getSelectionIndex();
			//					fLibraryViewer.remove(entry);
			//					Table table = fLibraryViewer.getTable();
			//					int itemCount = table.getItemCount();
			//					if (itemCount != 0) {
			//						index = index < itemCount ? index : itemCount - 1;
			//						newSel = new StructuredSelection(table.getItem(index).getData());
			//					}
			//				} else if (keyName != null && keyName.startsWith(IBuildEntry.JAR_PREFIX)) {
			//					// modification to source.{libname}.jar
			//					if (event.getOldValue() != null && event.getNewValue() != null)
			//						// renaming token
			//						fLibraryViewer.update(entry, null);
			//
			//					newSel = new StructuredSelection(entry);
			//				}
			//				fLibraryViewer.setSelection(newSel);
			//			} else if (keyName != null && keyName.equals(PROPERTY_JAR_ORDER)) {
			//				// account for change in jars compile order
			//				if (event.getNewValue() == null && event.getOldValue() != null)
			//					// removing token from jars compile order : do nothing
			//					return;
			//				if (event.getOldValue() != null && event.getNewValue() != null)
			//					// renaming token from jars compile order : do nothing
			//					return;
			//
			//				fLibraryViewer.refresh();
			//				updateDirectionalButtons();
//		}
	}




	public class AlvorPropertiesPage extends FormPage {
		public static final String PAGE_ID = "properties"; //$NON-NLS-1$
		private AlvorPropertiesSection section;
		
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

			section = new AlvorPropertiesSection(this, form.getBody());

			// TODO: We want to keep it expanded all the time...
			//			section.addExpansionListener(new ExpansionAdapter() {
			//				public void expansionStateChanged(ExpansionEvent e) {
			//					this.managedForm.reflow(true);
			//				}
			//			});		

			mform.addPart(section);
		}
	}

	
	
	protected void createPages() {
		// Create model management context here
		model = new AlvorPropertiesModel(this);
		
		// We need to create the model before creating pages
		super.createPages();
		
		
	/*	
	 * 
		// TODO IDocument management later
		//// assuming 'editorPart' is an instance of an org.eclipse.ui.IEditorPart
		//			
		 * 
		 * 
		 * try {
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

		return props;*/

		
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
	
	public AlvorPropertiesModel getModel() {
		return model;
	}
}



