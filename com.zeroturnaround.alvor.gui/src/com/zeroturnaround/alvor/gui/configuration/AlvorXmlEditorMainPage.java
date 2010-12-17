package com.zeroturnaround.alvor.gui.configuration;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class AlvorXmlEditorMainPage extends FormPage {
	private Form form = null;
	private FormToolkit toolkit = null;
	private HotspotsSectionPart hotspots = null;
	
	public AlvorXmlEditorMainPage(FormEditor editor, FormToolkit toolkit) {
		super(editor, "IIDEE", "Pealkiri");
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		this.toolkit = managedForm.getToolkit();
		this.form =  managedForm.getForm().getForm();
		form.setText("Alvor configuration for 'oratest'");
		form.setMessage("This is an error message", IMessageProvider.WARNING);
		
		toolkit.decorateFormHeading(form);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 15;
		//layout.verticalSpacing = 50;
		layout.topMargin = 15;
		form.getBody().setLayout(layout);
		
		System.out.println("SIIN");
		
		FormText rtext = toolkit.createFormText(form.getBody(), true);
		rtext.setText("Here is some plain text for the text to render.", false, true);
		rtext.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 2));
		
		//createHotspotsSection();
		
		hotspots = new HotspotsSectionPart(form.getBody(), managedForm.getToolkit());
		hotspots.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		
		createDatabaseSection();
		

		
		Hyperlink link = toolkit.createHyperlink(form.getBody(), "Open \"Markers\" view", SWT.WRAP);
		link.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 2));
		
		Section runSection = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR);
		runSection.setText("Running the checker");
		runSection.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.TOP));
		Composite runSectionClient = toolkit.createComposite(runSection);
		runSectionClient.setLayout(new GridLayout());
		runSection.setClient(runSectionClient);
		
		Button checkbox = toolkit.createButton(runSectionClient, "Check automatically after each save (checking starts after saving configuration)", SWT.CHECK);
		Button fullCheck = toolkit.createButton(runSectionClient, "Run clean check now", SWT.PUSH);
		Button clearButton = toolkit.createButton(runSectionClient, "Clear all check results", SWT.PUSH);
		
		Section resultsSection = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR);
		resultsSection.setText("Results");
		resultsSection.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.TOP));
		Composite resultsSectionClient = toolkit.createComposite(resultsSection);
		resultsSectionClient.setLayout(new GridLayout());
		resultsSection.setClient(resultsSectionClient);
		
		FormText resultText = toolkit.createFormText(resultsSectionClient, true);
		resultText.setText("Results are visible in Problems and Markers views.", false, true);
		
	}
	
	
	private void createHotspotsSection() {
		Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR);
		section.setText("Hotspots configuration");
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new GridLayout());
		section.setClient(sectionClient);
		Table hotspotsTable = toolkit.createTable(sectionClient, SWT.SINGLE);
		hotspotsTable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Button button2 = toolkit.createButton(sectionClient, "Analyze that", SWT.PUSH);
		
	}
	
	private void createDatabaseSection() {
		Section section2 = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR);
		section2.setText("Test DB configuration");
		section2.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		Composite sectionClient2 = toolkit.createComposite(section2);
		sectionClient2.setLayout(new GridLayout());
		section2.setClient(sectionClient2);
		Label label = toolkit.createLabel(sectionClient2, "Text field label:");
		Text text = toolkit.createText(sectionClient2, "");
	}
	
	@Override
	public void setFocus() {
		this.getManagedForm().getForm().setFocus();
	}

}

class CompositeClientSectionPart extends SectionPart {
	protected Composite client;
	public CompositeClientSectionPart(Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		this.client = toolkit.createComposite(this.getSection());
		this.getSection().setClient(client);
	}
	
}

class HotspotsSectionPart extends CompositeClientSectionPart {
	Table table;
	
	public HotspotsSectionPart(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, ExpandableComposite.TITLE_BAR);
		//getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		
		client.setLayout(new GridLayout());
		getSection().setText("Test DB configuration");
		
		table = toolkit.createTable(client, SWT.SINGLE);
		table.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Button button2 = toolkit.createButton(client, "Analyze that", SWT.PUSH);
	}
	
}
