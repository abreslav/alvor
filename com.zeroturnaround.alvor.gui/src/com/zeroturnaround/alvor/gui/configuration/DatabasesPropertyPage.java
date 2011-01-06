package com.zeroturnaround.alvor.gui.configuration;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.zeroturnaround.alvor.configuration.DataSourceProperties;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

public class DatabasesPropertyPage extends CommonPropertyPage {
	private Table table;
	private TableViewer tableViewer;
	private Text patternText;
	private Text driverText;
	private Text urlText;
	private Text usernameText;
	private Text passwordText;
	private List<DataSourceProperties> dataSources;

	@Override
	protected Control createContents(Composite parent) {
		ProjectConfiguration conf = readConfiguration();
		this.dataSources = conf.getDataSources();
		
		
		Composite body = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout(2, false);
		mainLayout.marginWidth = 0;
		body.setLayout(mainLayout);
		
		this.table = new Table(body, SWT.BORDER | SWT.FULL_SELECTION); // Table is actual widget
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData tableLD = new GridData(GridData.GRAB_HORIZONTAL, SWT.BEGINNING, true, false);
		tableLD.heightHint = 100;
		table.setLayoutData(tableLD);
		
		Button addButton = new Button(body, SWT.PUSH);
		addButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		addButton.setText("Add");
		
		// create viewer and populate with data
		this.tableViewer = createTableViewer(table); // TableViewer manages Table's data
		tableViewer.setInput(this.dataSources);
		
		
		// create detail form
		Composite form = new Composite(body, SWT.NONE);
		form.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout formLayout = new GridLayout(2, false);
		formLayout.marginWidth = 0;
		form.setLayout(formLayout);
		
		Label patternLabel = new Label(form, SWT.WRAP);
		patternLabel.setText("Connection pattern:");
		patternText = new Text(form, SWT.BORDER);
		patternText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		patternText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) { storeFormField(patternText); }
		});
		
		Label driverLabel = new Label(form, SWT.NONE);
		driverLabel.setText("Driver:");
		driverText = new Text(form, SWT.BORDER);
		driverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		driverText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) { storeFormField(driverText); }
		});
		
		
		Label urlLabel = new Label(form, SWT.NONE);
		urlLabel.setText("URL:");
		urlText = new Text(form, SWT.BORDER);
		urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		urlText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) { storeFormField(urlText); }
		});
		
		
		
		Label usernameLabel = new Label(form, SWT.NONE);
		usernameLabel.setText("Username:");
		usernameText = new Text(form, SWT.BORDER);
		usernameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) { storeFormField(usernameText); }
		});
		
		
		Label passwordLabel = new Label(form, SWT.NONE);
		passwordLabel.setText("Password:");
		passwordText = new Text(form, SWT.BORDER);
		passwordText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) { storeFormField(passwordText); }
		});
		
		return body;
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
		TableViewerColumn col = createTableViewerColumn(viewer, "Pattern", 200, 1);
		col.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(((DataSourceProperties)cell.getElement()).getPattern());
			}
		});
		
		col = createTableViewerColumn(viewer, "Url", 200, 0);
		col.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(String.valueOf(((DataSourceProperties)cell.getElement()).getUrl()));
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
		desc.setText("Here you can specify database specific preferences");
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
		DataSourceProperties prop = this.dataSources.get(table.getSelectionIndex());
		patternText.setText(prop.getPattern());
		driverText.setText(prop.getDriverName());
		urlText.setText(prop.getUrl());
		usernameText.setText(prop.getUserName());
		passwordText.setText(prop.getPassword());
	}
	
	private void storeFormField(Text text) {
		DataSourceProperties prop = this.dataSources.get(table.getSelectionIndex());
		if (text == patternText) { prop.setPattern(text.getText()); }
		if (text == driverText) { prop.setDriverName(text.getText()); }
		if (text == urlText) { prop.setUrl(text.getText()); }
		if (text == usernameText) { prop.setUserName(text.getText()); }
		if (text == passwordText) { prop.setPassword(text.getText()); }
		
		
		this.hasChanges = true;
		//this.tableViewer.setInput(dataSources);
	}
	
	@Override
	protected void mergeChanges(ProjectConfiguration base) {
		base.setDataSources(this.dataSources);
	}
}
