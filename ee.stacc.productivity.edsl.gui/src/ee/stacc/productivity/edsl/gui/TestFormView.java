package ee.stacc.productivity.edsl.gui;

//import org.eclipse.jface.resource.JFaceResources;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
		//GridLayout layout = new GridLayout();
		
		form.setText("Current properties:\n");
		
		form.getBody().setLayout(layout);
		
		for (Map.Entry<String, Object> entry : props.entrySet()) {
			toolkit.createLabel(form.getBody(), entry.getKey() +":"); //$NON-NLS-1$
			Text text = toolkit.createText(form.getBody(), entry.getValue().toString()); //$NON-NLS-1$
			TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
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
