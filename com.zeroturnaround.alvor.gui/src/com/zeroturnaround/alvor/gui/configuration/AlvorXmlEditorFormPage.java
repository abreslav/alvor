package com.zeroturnaround.alvor.gui.configuration;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.configuration.DataSourceProperties;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

/**
 * This editor page commits to ProjectConfiguration object, not to file
 * 
 *  No model listening is done -- changes to model should be pushed here using setConfiguration
 * @author Aivar
 *
 */
public class AlvorXmlEditorFormPage extends FormPage {
	private static final ILog LOG = Logs.getLog(AlvorXmlEditorFormPage.class);
	private static final String PAGE_ID = "com.zeroturnaround.alvor.AlvorXmlEditorMainPage"; 
	private Form form;
	private FormToolkit toolkit;
	private HotspotsSectionPart hotspots;
	private DatasourceSectionPart datasources;
	private RunningSectionPart runningPart;
	private ResultsSectionPart resultsPart;
	private ProjectConfiguration configuration;
	
	public AlvorXmlEditorFormPage(FormEditor editor, FormToolkit toolkit, 
			ProjectConfiguration configuration) {
		super(editor, PAGE_ID, "Configuration form");
		this.configuration = configuration;
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		this.toolkit = managedForm.getToolkit();
		this.form =  managedForm.getForm().getForm();
		form.setText("Alvor configuration for 'oratest'");
		form.setMessage("This is an error message", IMessageProvider.WARNING);
		toolkit.decorateFormHeading(form);
		
		// setup form layout
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 15;
		//layout.verticalSpacing = 50;
		layout.topMargin = 15;
		form.getBody().setLayout(layout);
		
		
		// intro text
		FormText rtext = toolkit.createFormText(form.getBody(), true);
		rtext.setText("Here is some plain text for the text to render.", false, true);
		rtext.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 2));
		
		// create all sections
		hotspots = new HotspotsSectionPart(form.getBody(), managedForm.getToolkit(), configuration);
		hotspots.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL_GRAB));
		managedForm.addPart(hotspots);
		
		datasources = new DatasourceSectionPart(form.getBody(), managedForm.getToolkit(), configuration);
		datasources.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));
		managedForm.addPart(datasources);
		
		runningPart = new RunningSectionPart(form.getBody(), managedForm.getToolkit(), configuration);
		runningPart.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));
		managedForm.addPart(runningPart);
		
		resultsPart = new ResultsSectionPart(form.getBody(), managedForm.getToolkit(), configuration);
		resultsPart.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));
		managedForm.addPart(runningPart);
	}
	
	
	@Override
	public void setFocus() {
		this.hotspots.setFocus();
	}
	
//	@Override
//	public void doSave(IProgressMonitor monitor) {
//		super.doSave(monitor);
//	}
}

class CommonClientSectionPart extends SectionPart implements ModifyListener {
	protected Composite client;
	protected ProjectConfiguration configuration;
	protected CommonClientSectionPart(Composite parent, FormToolkit toolkit, int style, 
			ProjectConfiguration configuration) {
		super(parent, toolkit, style);
		this.client = toolkit.createComposite(this.getSection());
		this.getSection().setClient(client);
		this.configuration = configuration;
	}
	
	@Override // for ModifyListener
	public void modifyText(ModifyEvent e) {
		this.markDirty();
	}
	
	@Override
	public void initialize(IManagedForm form) {
		super.initialize(form);
	}
	
}

class DatasourceSectionPart extends CommonClientSectionPart {
	private Table table;
	private Text driverText;
	private Text urlText;
	private Text usernameText;
	private Text passwordText;
	
	public DatasourceSectionPart(Composite parent, FormToolkit toolkit, ProjectConfiguration configuration) {
		super(parent, toolkit, ExpandableComposite.TITLE_BAR, configuration);
		getSection().setText("Databases");
		
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		client.setLayout(layout);
		
		toolkit.createLabel(client, "JDBC driver:");
		driverText = toolkit.createText(client, "");
		driverText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		driverText.addModifyListener(this);
		
		toolkit.createLabel(client, "Database URL:");
		urlText = toolkit.createText(client, "");
		urlText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		urlText.addModifyListener(this);
		
		toolkit.createLabel(client, "Username:");
		usernameText = toolkit.createText(client, "");
		usernameText.addModifyListener(this);
		
		toolkit.createLabel(client, "Password:");
		passwordText = toolkit.createText(client, "");
		passwordText.addModifyListener(this);
	}
	
	@Override
	public void refresh() {
		DataSourceProperties dsp = this.configuration.getDataSources().get(0);
		driverText.setText(dsp.getDriverName());
		super.refresh();
	}
	
	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		this.getManagedForm().dirtyStateChanged();
	}
	
}

class RunningSectionPart extends CommonClientSectionPart {
	
	public RunningSectionPart(Composite parent, FormToolkit toolkit, ProjectConfiguration configuration) {
		super(parent, toolkit, ExpandableComposite.TITLE_BAR, configuration);
		client.setLayout(new GridLayout());
		getSection().setText("Running the analyzer");
		
		Button checkbox = toolkit.createButton(client, "Check automatically after each save (checking starts after saving configuration)", SWT.CHECK);
		Button fullCheck = toolkit.createButton(client, "Run clean check now", SWT.PUSH);
		Button clearButton = toolkit.createButton(client, "Clear checking results", SWT.PUSH);
	}
}

class ResultsSectionPart extends CommonClientSectionPart {
	
	public ResultsSectionPart(Composite parent, FormToolkit toolkit, ProjectConfiguration configuration) {
		super(parent, toolkit, ExpandableComposite.TITLE_BAR, configuration);
		client.setLayout(new GridLayout());
		getSection().setText("Results");
		
		FormText resultText = toolkit.createFormText(client, true);
		resultText.setText("Results are visible in Problems and Markers views.", false, true);
		
		Hyperlink markersLink = toolkit.createHyperlink(client, "Open 'Markers' view", 0);
		markersLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent event) {
				if (ResultsSectionPart.this.getManagedForm() == null) {
					System.out.println("ou nou");
				}
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView("org.eclipse.ui.views.AllMarkersView");
					// IPageLayout.ID_OUTLINE
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
	}
}

class HotspotsSectionPart extends CommonClientSectionPart {
	private Table table;
	
	public HotspotsSectionPart(Composite parent, FormToolkit toolkit, ProjectConfiguration configuration) {
		super(parent, toolkit, ExpandableComposite.TITLE_BAR, configuration);
		getSection().setText("Hotspots");
		
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		client.setLayout(layout);

		
		table = toolkit.createTable(client, SWT.SINGLE);
		table.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
		
		Composite buttonPanel = toolkit.createComposite(client);
		//buttonPanel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
		buttonPanel.setLayout(new FillLayout(SWT.VERTICAL));
		Button addButton = toolkit.createButton(buttonPanel, "Add", SWT.PUSH);
		Button removeButton = toolkit.createButton(buttonPanel, "Remove", SWT.PUSH);
		
//		addButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				HotspotsSectionPart.this.markDirty();
//			}
//		});
		
	}
	
}
