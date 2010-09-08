package com.zeroturnaround.alvor.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.texteditor.IDocumentProvider;

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
	private TextEditor texteditor;
	
	public class AlvorPropertiesModel {
		public final String sdbdrivername =	"DBDriverName";
		public final String sdburl = "DBUrl";
		public final String sdbusername = "DBUsername";
		public final String sdbpassword = "DBPassword"; 
		public final String shotspots = "hotspots";
		
		private AlvorPropertiesEditor editor;
		
		private String dbdrivername = null;
		private String dburl = null;
		private String dbusername = null;
		private String dbpassword = null;
		private String hotspots = null;
		
		public String getDbdrivername() {
			return dbdrivername;
		}
		
		public AlvorPropertiesModel(AlvorPropertiesEditor editor) {
			// Do we maybe need this for later, signaling or something?
			this.editor = editor;
		}

		public void refresh() {
			Properties props = loadProps();
				
			// (getProperty defaults to null)
			dbdrivername = props.getProperty(sdbdrivername);
			dburl = props.getProperty(sdburl);
			dbusername = props.getProperty(sdbusername);
			dbpassword = props.getProperty(sdbpassword);
			hotspots = props.getProperty(shotspots);
		}

		private Properties loadProps() {
			TextEditor texteditor = editor.getTextEditor();
			Properties props = new Properties();
			
			if (null == editor)
				System.err.println("DEBUG: failed to load model due to editor not available!");
			else {
				IDocumentProvider provider = texteditor.getDocumentProvider();
				IDocument document = provider.getDocument(texteditor.getEditorInput());

				try {
					props.load(new ByteArrayInputStream(document.get().getBytes("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return props;
		}
		
		private void saveProps(Properties props) {
			TextEditor texteditor = editor.getTextEditor();
			
			IDocumentProvider provider = texteditor.getDocumentProvider();
			IDocument document = provider.getDocument(texteditor.getEditorInput());

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
			if (dburl == null)
				refresh();
			return dburl;
		}
		
		public String getDbusername() {
			if (dbusername == null)
				refresh();
			return dbusername;		
		}
		

		public String getDbpassword() {
			if (dbpassword == null)
				refresh();
			return dbpassword;		
		}
		
//		public List<Hotspot> getHotspots() {
		public String getHotspots() {
			if (hotspots == null)
				refresh();
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
		
		public void setDbusername(String dbusername) {
			this.dbusername = dbusername;
			setProperty(sdbusername, dbusername);
		}

		public void setDbpassword(String dbpassword) {
			this.dbpassword = dbpassword;
			setProperty(sdbpassword, dbpassword);
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

			getSection().setText("General information");
			getSection().setDescription("This describes the basic information necessary for the Alvor SQL checker");

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
			AlvorPropertiesModel model = getPropertiesModel();
			Label label = null;
			Text text = null;
			TableWrapData td = null;
			
			Composite container = toolkit.createComposite(section);
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 3;
			container.setLayout(layout);

			// Make this a section more?
//			label = toolkit.createLabel(container, "Dynamic testing database");
//			td = new TableWrapData(TableWrapData.FILL_GRAB);
//			td.colspan = 2;
//			label.setLayoutData(td);

			label = toolkit.createLabel(container, "Database URL:");
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			text = toolkit.createText(container, model.getDburl());
			text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			
			label = toolkit.createLabel(container, "This is the information required to access the" +
					"(optional) database for dynamic testing. Currently Alvor " +
					"supports accessing only one database per project being checked");
			td = new TableWrapData(TableWrapData.FILL_GRAB);
			td.rowspan = 3;
			label.setLayoutData(td);

			label = toolkit.createLabel(container, "Database username:");
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			text = toolkit.createText(container, model.getDbusername());
			text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			label = toolkit.createLabel(container, "Database password:");
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			text = toolkit.createText(container, model.getDbpassword());
			text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			label = toolkit.createLabel(container, "Hotspots:");
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			text = toolkit.createText(container, model.getHotspots());			
			text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			
			label = toolkit.createLabel(container, "Hotspots define the entry points in code for SQL strings to be checked. For now, hotspots should be given as a semicolon-separated list of entries as such: \"<package>,<method>,<argument number>\" ");
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

//			OverviewPage_extensionContent=<form>\
//			<p>This plug-in may define extensions and extension points:</p>\
//			<li style="image" value="page" bindent="5"><a href="extensions">Extensions</a>: declares contributions this plug-in makes to the platform.</li>\
//			<li style="image" value="page" bindent="5"><a href="ex-points">Extension Points</a>: declares new function points this plug-in adds to the platform.</li>\
//			</form>

//			private void createExtensionSection(IManagedForm managedForm, Composite parent, FormToolkit toolkit) {
//				String sectionTitle = PDEUIMessages.ManifestEditor_ExtensionSection_title;
//				Section section = createStaticSection(toolkit, parent, sectionTitle);
//
//				Composite container = createStaticSectionClient(toolkit, section);
//
//				FormText text = createClient(container, isFragment() ? PDEUIMessages.OverviewPage_fExtensionContent : PDEUIMessages.OverviewPage_extensionContent, toolkit);
//				PDELabelProvider lp = PDEPlugin.getDefault().getLabelProvider();
//				text.setImage("page", lp.get(PDEPluginImages.DESC_PAGE_OBJ, SharedLabelProvider.F_EDIT)); //$NON-NLS-1$
//
//				section.setClient(container);
//			}


//			private void fillBody(IManagedForm managedForm, FormToolkit toolkit) {
//				Composite body = managedForm.getForm().getBody();
//				body.setLayout(FormLayoutFactory.createFormTableWrapLayout(true, 2));
//
//				Composite left = toolkit.createComposite(body);
//				left.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
//				left.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
//				if (isFragment())
//					fInfoSection = new FragmentGeneralInfoSection(this, left);
//				else
//					fInfoSection = new PluginGeneralInfoSection(this, left);
//				managedForm.addPart(fInfoSection);
//				if (isBundle())
//					managedForm.addPart(new ExecutionEnvironmentSection(this, left));
//
//				Composite right = toolkit.createComposite(body);
//				right.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
//				right.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
//				createContentSection(managedForm, right, toolkit);
//				if (isEditable() || getPDEEditor().hasInputContext(PluginInputContext.CONTEXT_ID))
//					createExtensionSection(managedForm, right, toolkit);
//				if (isEditable()) {
//					createTestingSection(managedForm, isBundle() ? right : left, toolkit);
//				}
//				if (isEditable())
//					createExportingSection(managedForm, right, toolkit);
//			}

			
			
			//			/*	text.addModifyListener(new ModifyListener() {
			//					public void modifyText(ModifyEvent e) {
			//						setDirty(true);
			//					}
			//				});*/
			
			section.setLayoutData(new TableWrapData());
			section.setClient(container);
		}
	}




	public class AlvorPropertiesPage extends FormPage {
		public static final String PAGE_ID = "properties"; //$NON-NLS-1$
		private AlvorPropertiesSection section;
		
		public AlvorPropertiesPage(FormEditor editor) {
			super(editor, PAGE_ID, "Alvor configuration");
		}
		
		protected void createFormContent(IManagedForm mform) {
			super.createFormContent(mform);
//			FormToolkit toolkit = mform.getToolkit();
			ScrolledForm form = mform.getForm();
			form.getBody().setLayout(new TableWrapLayout());	
			//			form.setText(PDEUIMessages.BuildEditor_BuildPage_title);


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
		
		super.createPages();	
	}
	
	@Override
	protected void addPages() {
		try {
			texteditor = new TextEditor();
			int index = addPage(texteditor, getEditorInput());
			setPageText(index, texteditor.getTitle());
			
			// This is added after the texteditor to avoid a round trip to the model
			addPage(0, new AlvorPropertiesPage(this));			
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
	
	public TextEditor getTextEditor() {
		return texteditor;
	}
}



