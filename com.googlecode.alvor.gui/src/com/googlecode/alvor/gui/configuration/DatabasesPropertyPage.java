package com.googlecode.alvor.gui.configuration;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.googlecode.alvor.configuration.DataSourceProperties;
import com.googlecode.alvor.configuration.ProjectConfiguration;

public class DatabasesPropertyPage extends CommonPropertyPage {
	private Table table;
	private TableViewer tableViewer;
	//private Text patternText;
	private Text driverText;
	private Text urlText;
	private Text usernameText;
	private Text passwordText;
	private List<DataSourceProperties> dataSources;
	
	private ModifyListener modifyListener; 


	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		
		ProjectConfiguration conf = readConfiguration();
		this.dataSources = conf.getDataSources();
		
		
		Composite body = new Composite(parent, SWT.NONE);
		RowLayout mainLayout = new RowLayout(SWT.VERTICAL);
		mainLayout.marginWidth = 0;
		mainLayout.fill = true;
		mainLayout.pack = true;
		body.setLayout(mainLayout);
		
		createUpperBlock(body);
		createDetailForm(body);
		
		tryToSelectRow(0);
		return body;
	}
	
	private void createUpperBlock(Composite parent) {
		Composite upperBlock = new Composite(parent, SWT.NONE);
		GridLayout upperLayout = new GridLayout(2, false);
		upperLayout.marginWidth = 0;
		upperBlock.setLayout(upperLayout);
		
		this.table = new Table(upperBlock, SWT.BORDER | SWT.FULL_SELECTION); 
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData tableLD = new GridData(GridData.FILL_HORIZONTAL);
		tableLD.heightHint = 100;
		table.setLayoutData(tableLD);
		
		Composite buttonBlock = new Composite(upperBlock, SWT.NONE);
		buttonBlock.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		buttonBlock.setLayoutData(new GridData(GridData.BEGINNING));
		buttonBlock.setLayout(new FillLayout(SWT.VERTICAL));
		Button addButton = new Button(buttonBlock, SWT.PUSH);

		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addNewDataSource();
			}
		});
		
		Button removeButton = new Button(buttonBlock, SWT.PUSH);
		//addButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedDataSource();
			}
		});
		
		// create viewer and populate with data
		this.tableViewer = createTableViewer(table); // TableViewer manages Table's data
		tableViewer.setInput(this.dataSources);
		
	}
	
	private void createDetailForm(Composite parent) {
		Composite form = new Composite(parent, SWT.NONE);
		//form.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout formLayout = new GridLayout(2, false);
		formLayout.marginWidth = 0;
		form.setLayout(formLayout);
		
		this.modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Text text = (Text)e.widget;
				storeFormField(text);
			}
		};
		
//		Label patternLabel = new Label(form, SWT.WRAP);
//		patternLabel.setText("Connection pattern:");
//		patternText = new Text(form, SWT.BORDER);
//		patternText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		patternText.addModifyListener(modifyListener);
		
		Label driverLabel = new Label(form, SWT.NONE);
		driverLabel.setText("Driver:");
		driverText = new Text(form, SWT.BORDER);
		driverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		driverText.addModifyListener(modifyListener);
		
		Label urlLabel = new Label(form, SWT.NONE);
		urlLabel.setText("URL:");
		urlText = new Text(form, SWT.BORDER);
		urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		urlText.addModifyListener(modifyListener);
		
		Label usernameLabel = new Label(form, SWT.NONE);
		usernameLabel.setText("Username:");
		usernameText = new Text(form, SWT.BORDER);
		usernameText.addModifyListener(modifyListener);
		
		Label passwordLabel = new Label(form, SWT.NONE);
		passwordLabel.setText("Password:");
		passwordText = new Text(form, SWT.BORDER);
		passwordText.addModifyListener(modifyListener);
	}

	private TableViewer createTableViewer(Table table) {
		final TableViewer viewer = new TableViewer(table);
		
		// Content provider translates any object into array of (domain) objects 
		// (here each item of array corresponds to one row)
		// ArrayContentProvider knows how to translate arrays or lists (of domain objects) into arrays 
		viewer.setContentProvider(new ArrayContentProvider());

		// later, when tableViewer.setInput(collectionOfDomainObjects) is used,
		// then given input is first translated by content provider and 
		// each individual is processed by label provider (on-demand)
		
		// setup columns
		TableViewerColumn col = null;

//		col = createTableViewerColumn(viewer, "Pattern", 200, 1);
//		col.setLabelProvider(new CellLabelProvider() {
//			@Override
//			public void update(ViewerCell cell) {
//				String text = ((DataSourceProperties)cell.getElement()).getPattern();
//				if (text.trim().isEmpty()) {
//					text = "<new pattern>";
//				}
//				cell.setText(text);
//			}
//		});
		
		col = createTableViewerColumn(viewer, "Url", 400, 0);
		col.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				String text = ((DataSourceProperties)cell.getElement()).getUrl();
				if (text.trim().isEmpty()) {
					text = "<new url>";
				}
				cell.setText(text);
				viewer.refresh();
			}
		});
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateForm();
			}
		});
		
		return viewer;
	}
	
	@Override
	protected Label createDescriptionLabel(Composite parent) {
		Label desc = new Label(parent, SWT.WRAP);
		desc.setText("Here you can specify database connections for testing SQL validity.\n\n" +
				"NB! Currently Alvor supports only single connection (Oracle or MySQL)");
		return desc;
	}
	

	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int width, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		return viewerColumn;
	}

	private void updateForm() {
		int i = table.getSelectionIndex();
		if (i > -1 && this.dataSources.size() > i) {
			DataSourceProperties prop = this.dataSources.get(i);
	//		updateText(patternText, prop.getPattern());
			updateText(driverText, prop.getDriverName());
			updateText(urlText, prop.getUrl());
			updateText(usernameText, prop.getUserName());
			updateText(passwordText, prop.getPassword());
		} 
		else { // nothing is selected
			//updateText(patternText, "");
			updateText(driverText, "");
			updateText(urlText, "");
			updateText(usernameText, "");
			updateText(passwordText, "");
		}
	}
	
	private void storeFormField(Text text) {
		
		// prepare new place, if there are no dataSources yet (and nothing can be selected)
		// and users starts editing before pressing "Add"
		if (dataSources.size() == 0) {
			dataSources.add(new DataSourceProperties("", "", "", "", ""));
		}
		int i = table.getSelectionIndex();
		if (i == -1) {
			i = 0;
		}
		
		DataSourceProperties prop = this.dataSources.get(i);
		//if (text == patternText) { prop.setPattern(text.getText()); }
		if (text == driverText) { prop.setDriverName(text.getText()); }
		if (text == urlText) { prop.setUrl(text.getText()); }
		if (text == usernameText) { prop.setUserName(text.getText()); }
		if (text == passwordText) { prop.setPassword(text.getText()); }
		
		this.hasChanges = true;
		this.tableViewer.refresh();
	}
	
	@Override
	protected void mergeChanges(ProjectConfiguration base) {
		base.setDataSources(this.dataSources);
	}
	
	private void removeSelectedDataSource() {
		
		int i = table.getSelectionIndex();
		if (i >= 0) {
			dataSources.remove(i);
		}
		
		tryToSelectRow(0);
		tableViewer.refresh();
		updateForm();
	}
	
	private void addNewDataSource() {
		if (dataSources.size() >= 1) {
			return; // TODO temporary
		}
		DataSourceProperties prop = new DataSourceProperties("", "", "", "", "");
		dataSources.add(prop);
		tableViewer.refresh();
		tryToSelectRow(dataSources.size()-1);
		
	}
	
	private void tryToSelectRow(int rowNo) {
		Object element = tableViewer.getElementAt(rowNo);
		if (element != null) {
			tableViewer.setSelection(new StructuredSelection(element), true);
		}
		else {
			updateForm();
		}
	}
	
	private void updateText(Text text, String content) {
		text.removeModifyListener(this.modifyListener); // don't want infinite loop here
		try {
			text.setText(content);
		} finally {
			text.addModifyListener(this.modifyListener);
		}
	}
}
