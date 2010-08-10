package ee.stacc.productivity.edsl.gui;

//import org.eclipse.jface.resource.JFaceResources;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

//import org.eclipse.swt.widgets.Label;
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
		String formText = new String();
		
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		Map<String, Object> props = null;
		try {
			props = OptionLoader.getElementSqlCheckerProperties(GuiUtil.getSelectedJavaProject());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		formText += "Current properties:\n\n";
		
		for (Map.Entry<String, Object> entry : props.entrySet()) {
			formText += entry.getKey() + ": " + entry.getValue().toString() + "\n";
		}

		form.setText(formText);
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
