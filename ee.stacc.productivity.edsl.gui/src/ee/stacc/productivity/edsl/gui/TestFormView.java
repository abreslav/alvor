package ee.stacc.productivity.edsl.gui;

//import org.eclipse.jface.resource.JFaceResources;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.ui.forms.FormColors;
//import org.eclipse.ui.forms.article.FormArticlePlugin;
//import org.eclipse.ui.forms.events.ExpansionAdapter;
//import org.eclipse.ui.forms.events.ExpansionEvent;
//import org.eclipse.ui.forms.events.HyperlinkAdapter;
//import org.eclipse.ui.forms.events.HyperlinkEvent;
//import org.eclipse.ui.forms.widgets.ExpandableComposite;
//import org.eclipse.ui.forms.widgets.FormText;

import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
//import org.eclipse.ui.forms.widgets.Hyperlink;
//import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
//import org.eclipse.ui.forms.widgets.Section;
//import org.eclipse.ui.forms.widgets.TableWrapData;
//import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

import ee.stacc.productivity.edsl.main.OptionLoader;
import ee.stacc.productivity.edsl.gui.GuiUtil;


/*
 * These are the component of current sqlchecker.properties to deal with:
 * 
 * In future, we may have multiple instances of DB*:
 * DBDriverName=oracle.jdbc.OracleDriver
 * DBDriverName=org.hsqldb.jdbc.JDBCDriver
 * 	- this is a class which can be selected from somewhere... ?
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
public class TestFormView extends ViewPart {
	 private FormToolkit toolkit;
	 private ScrolledForm form;

	 /**
	  * The constructor.
	  */
	 public TestFormView() {
	 }
	 
	@Override
	public void createPartControl(Composite parent) {
		Map<String, Object> props = null;
		
		try {
			props = OptionLoader.getElementSqlCheckerProperties(GuiUtil.getSelectedJavaElements().get(0));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		
		form.setText("Current properties:\n");
		
		form.getBody().setLayout(layout);
		
		for (Map.Entry<String, Object> entry : props.entrySet()) {
			TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
			Label label = toolkit.createLabel(form.getBody(), entry.getKey() +":"); //$NON-NLS-1$
			td = new TableWrapData(TableWrapData.RIGHT);
			label.setLayoutData(td);
			td = new TableWrapData(TableWrapData.FILL_GRAB);
			Text text = toolkit.createText(form.getBody(), entry.getValue().toString()); //$NON-NLS-1$
			text.setLayoutData(td);
		}
	}

	@Override
	public void setFocus() {
		  form.setFocus();
	}
	
	 /**
	  * Disposes the toolkit
	  */
	 public void dispose() {
	  toolkit.dispose();
	  super.dispose();
	 }
}
