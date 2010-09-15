package com.zeroturnaround.alvor.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
	private AlvorPropertiesPage propertiespage;
	private TextEditor texteditor;
	
	public class AlvorPropertiesModel {
		public final String sdbdrivername =	"DBDriverName";
		public final String sdburl = "DBUrl";
		public final String sdbusername = "DBUsername";
		public final String sdbpassword = "DBPassword"; 
		public final String shotspots = "hotspots";
		
		private AlvorPropertiesEditor editor;
		private IDocument document;
		
		private String dbdrivername = null;
		private String dburl = null;
		private String dbusername = null;
		private String dbpassword = null;
		private String hotspots = null;
		
		public AlvorPropertiesModel(AlvorPropertiesEditor editor) {
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
			Properties props = null;
			
			if (document == null) 
				loadDocument();
			
			// Only if document has been successfully loaded
			if (document != null) {
				props = new Properties();
				
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
			System.err.println("DEBUG: Saving document!");

			if (document == null) 
				loadDocument();

			if (document != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					props.store(baos, null);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				document.set(baos.toString());
			}
		}
		
		private void loadDocument() {
			System.err.println("DEBUG: Loading document!");
			
			TextEditor texteditor = editor.getTextEditor();
			
			if (null == texteditor)
				System.err.println("DEBUG: failed to load model due to texteditor not available!");
			else {
				IDocumentProvider provider = texteditor.getDocumentProvider();
				document = provider.getDocument(texteditor.getEditorInput());
			}
		}
		
		private void setProperty(String key, String value) {
			Properties props = loadProps();
			
			if (! props.getProperty(key).equals(value)) {
				props.setProperty(key, value);
				saveProps(props);
			}
		}
		
		public void setAll(String dbdrivername,
				String dburl,
				String dbusername,
				String dbpassword,
				String hotspots) {
			setProperty(sdbdrivername, dbdrivername);
			setProperty(sdburl, dburl);
			setProperty(sdbusername, dbusername);
			setProperty(sdbpassword, dbpassword);
			setProperty(shotspots, hotspots);

			System.err.println("DEBUG: setting private model fields");
			
			this.dbdrivername = dbdrivername;
			this.dburl = dburl;
			this.dbusername = dbusername;
			this.dbpassword = dbpassword;
			this.hotspots = hotspots;
		}		
		
		public String getDbdrivername() {
			if (dbdrivername == null)
				refresh();
			return dbdrivername;
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
		
		public void setDbdrivername(String dbdrivername) {
			setProperty(sdbdrivername, dbdrivername);
			this.dbdrivername = dbdrivername;
		}
		
		public void setDburl(String dburl) {
			setProperty(sdburl, dburl);
			this.dburl = dburl;
		}
		
		public void setDbusername(String dbusername) {
			setProperty(sdbusername, dbusername);
			this.dbusername = dbusername;
		}

		public void setDbpassword(String dbpassword) {
			setProperty(sdbpassword, dbpassword);
			this.dbpassword = dbpassword;
		}
		
//		public void setHotspots(List<Hotspot> hotspots) {
		public void setHotspots(String hotspots) {
			setProperty(shotspots, hotspots);
			this.hotspots = hotspots;
		}
	}
	
	
	
	public class AlvorPropertiesSection extends SectionPart {
		private FormPage page;
		private Text fdbdrivername;
		private Text fdburl;
		private Text fdbusername;
		private Text fdbpassword;
		private Text fhotspots;
		
		AlvorPropertiesSection(FormPage page, Composite parent) {
			super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION|Section.TITLE_BAR|Section.EXPANDED);
			this.page = page;

			getSection().setText("General information");
			getSection().setDescription("This describes the basic information necessary for the Alvor SQL checker");

			createClient(getSection(), page.getManagedForm().getToolkit());
		}

		private void dialogChanged() {
			System.err.println("DEBUG: Dialog changed");
			AlvorPropertiesModel model = getPropertiesPage().getPropertiesModel();
			
			// Do I need these null checks? 
			if (model != null &&
					fdbdrivername != null && fdburl != null && 
					fdbusername != null && fdbpassword != null && fhotspots != null) {
			
				System.err.println("Setting all in model...");
				
				// Model should always be updated, no problem
				model.setAll(fdbdrivername.getText(), 
						fdburl.getText(), 
						fdbusername.getText(),
						fdbpassword.getText(), 
						fhotspots.getText()); 
			}
		}
		
		public void modelChanged() {
			System.out.println("DEBUG: model changed, updating form!");
			
			AlvorPropertiesModel model = getPropertiesPage().getPropertiesModel();
			
			if (model != null) {
				if (! fdbdrivername.getText().equals(model.getDbdrivername())) {
					fdbdrivername.setText(model.getDbdrivername());
					System.out.println("Trying to set drivername");
				}
				
				if (! fdburl.getText().equals(model.getDburl())) {
					fdburl.setText(model.getDburl());
					System.out.println("Trying to set dburl");
				}
				else {
					System.out.println("Ignored setting dburl");
					System.out.println("form: " + fdburl.getText() + " model: " + model.getDburl());
				}

				if (! fdbusername.getText().equals(model.getDbusername()))
					fdbusername.setText(model.getDbusername());

				if (! fdbpassword.getText().equals(model.getDbpassword()))
					fdbpassword.setText(model.getDbpassword());

				if (! fhotspots.getText().equals(model.getHotspots()))
					fhotspots.setText(model.getHotspots());
			}
			else
				System.out.println("DEBUG: Model changed, but null?");
		}
		
		private AlvorPropertiesPage getPropertiesPage() {
			if (page instanceof AlvorPropertiesPage)
				return (AlvorPropertiesPage) page;
			else
				return null;
		}
		
		public void createClient(final Section section, FormToolkit toolkit) {
			Label label = null;
			TableWrapData td = null;

			ModifyListener listener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					System.out.println(e.toString());
					dialogChanged();
				}
			};
			
			Composite container = toolkit.createComposite(section);
			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 3;
			container.setLayout(layout);
			
			// Make this a section more?
//			label = toolkit.createLabel(container, "Dynamic testing database");
//			td = new TableWrapData(TableWrapData.FILL_GRAB);
//			td.colspan = 2;
//			label.setLayoutData(td);

			label = toolkit.createLabel(container, "Database driver");
			label.setLayoutData(new TableWrapData());
			fdbdrivername = toolkit.createText(container, "");
			fdbdrivername.setLayoutData(new TableWrapData());
			
			label = toolkit.createLabel(container, "This is the information required to access the" +
					"(optional) database for dynamic testing. Currently Alvor " +
					"supports accessing only one database per project being checked", SWT.WRAP);
			td = new TableWrapData(TableWrapData.FILL_GRAB); td.rowspan = 4;
			label.setLayoutData(td);

			label = toolkit.createLabel(container, "Database URL:");
			label.setLayoutData(new TableWrapData());
			fdburl = toolkit.createText(container, "");
			fdburl.setLayoutData(new TableWrapData());
			label = toolkit.createLabel(container, "Database username:");
			label.setLayoutData(new TableWrapData());
			fdbusername = toolkit.createText(container, "");
			fdbusername.setLayoutData(new TableWrapData());
			label = toolkit.createLabel(container, "Database password:");
			label.setLayoutData(new TableWrapData());
			fdbpassword = toolkit.createText(container, "");
			fdbpassword.setLayoutData(new TableWrapData());
			label = toolkit.createLabel(container, "Hotspots:");
			label.setLayoutData(new TableWrapData());
			fhotspots = toolkit.createText(container, "");			
			fhotspots.setLayoutData(new TableWrapData());
			
			label = toolkit.createLabel(container, "Hotspots define the entry points in code for SQL strings to be checked. For now, hotspots should be given as a semicolon-separated list of entries as such: \"<package>,<method>,<argument number>\" ", SWT.WRAP);
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			modelChanged();
			
			fdbdrivername.addModifyListener(listener);
			fdburl.addModifyListener(listener);
			fdbusername.addModifyListener(listener);
			fdbpassword.addModifyListener(listener);
			fhotspots.addModifyListener(listener);

			// These comments are regarding adding styled content in the fashion of the other editors
			/*
			OverviewPage_extensionContent=<form>\
			<p>This plug-in may define extensions and extension points:</p>\
			<li style="image" value="page" bindent="5"><a href="extensions">Extensions</a>: declares contributions this plug-in makes to the platform.</li>\
			<li style="image" value="page" bindent="5"><a href="ex-points">Extension Points</a>: declares new function points this plug-in adds to the platform.</li>\
			</form>

			private void createExtensionSection(IManagedForm managedForm, Composite parent, FormToolkit toolkit) {
				String sectionTitle = PDEUIMessages.ManifestEditor_ExtensionSection_title;
				Section section = createStaticSection(toolkit, parent, sectionTitle);

				Composite container = createStaticSectionClient(toolkit, section);

				FormText text = createClient(container, isFragment() ? PDEUIMessages.OverviewPage_fExtensionContent : PDEUIMessages.OverviewPage_extensionContent, toolkit);
				PDELabelProvider lp = PDEPlugin.getDefault().getLabelProvider();
				text.setImage("page", lp.get(PDEPluginImages.DESC_PAGE_OBJ, SharedLabelProvider.F_EDIT)); //$NON-NLS-1$

				section.setClient(container);
			}


			private void fillBody(IManagedForm managedForm, FormToolkit toolkit) {
				Composite body = managedForm.getForm().getBody();
				body.setLayout(FormLayoutFactory.createFormTableWrapLayout(true, 2));

				Composite left = toolkit.createComposite(body);
				left.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
				left.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
				if (isFragment())
					fInfoSection = new FragmentGeneralInfoSection(this, left);
				else
					fInfoSection = new PluginGeneralInfoSection(this, left);
				managedForm.addPart(fInfoSection);
				if (isBundle())
					managedForm.addPart(new ExecutionEnvironmentSection(this, left));

				Composite right = toolkit.createComposite(body);
				right.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
				right.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
				createContentSection(managedForm, right, toolkit);
				if (isEditable() || getPDEEditor().hasInputContext(PluginInputContext.CONTEXT_ID))
					createExtensionSection(managedForm, right, toolkit);
				if (isEditable()) {
					createTestingSection(managedForm, isBundle() ? right : left, toolkit);
				}
				if (isEditable())
					createExportingSection(managedForm, right, toolkit);
			}
			*/
			section.setLayoutData(new TableWrapData());
			section.setClient(container);
		}
	}




	public class AlvorPropertiesPage extends FormPage implements IDocumentListener {
		public static final String PAGE_ID = "properties"; //$NON-NLS-1$
		private AlvorPropertiesSection section;
		
		public AlvorPropertiesPage(FormEditor editor) {
			super(editor, PAGE_ID, "Alvor configuration");
		}
		
		private AlvorPropertiesModel getPropertiesModel() {
			FormEditor editor = getEditor();
			if (editor instanceof AlvorPropertiesEditor)
				return ((AlvorPropertiesEditor) editor).getModel();
			else
				return null;
		}
		
		public void documentAboutToBeChanged(DocumentEvent event) {
			//			
		}
		
		public void documentChanged(DocumentEvent event) {
			System.out.println("Document changed " + event.toString());
			
			// Update model from document
			getPropertiesModel().refresh();
			
			// Update form from model
			section.modelChanged();
		}
		
		protected void createFormContent(IManagedForm mform) {
			super.createFormContent(mform);
			ScrolledForm form = mform.getForm();
			form.getBody().setLayout(new TableWrapLayout());	

			section = new AlvorPropertiesSection(this, form.getBody());

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
			propertiespage = new AlvorPropertiesPage(this);
			addPage(0, propertiespage);
			
			texteditor.getDocumentProvider().getDocument(getEditorInput()).addDocumentListener(propertiespage);
			
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
		texteditor.doSave(monitor);
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

	public AlvorPropertiesModel getModel() {
		return model;
	}
	
	public TextEditor getTextEditor() {
		return texteditor;
	}
}



