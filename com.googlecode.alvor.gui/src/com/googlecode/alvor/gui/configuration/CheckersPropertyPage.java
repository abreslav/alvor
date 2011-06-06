package com.googlecode.alvor.gui.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.googlecode.alvor.checkers.CheckerInfo;
import com.googlecode.alvor.checkers.CheckersManager;
import com.googlecode.alvor.configuration.CheckerConfiguration;
import com.googlecode.alvor.configuration.ProjectConfiguration;

public class CheckersPropertyPage extends CommonPropertyPage {
//	private Table table;
	private org.eclipse.swt.widgets.List checkersList;
//	private TableViewer tableViewer;
	//private Text patternText;
	private Text driverText;
	private Text urlText;
	private Text usernameText;
	private Text passwordText;
	private Text patternsText;
	private Combo checkerNameCombo;
	private List<CheckerConfiguration> checkerConfs;
	private Map<String, CheckerInfo> checkerInfos = new HashMap<String, CheckerInfo>();
	
	private final ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			fieldChanged(e.widget);
		}
	}; 
	
	private final SelectionListener listSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			updateForm();
		}
	};


	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		
		ProjectConfiguration conf = readConfiguration();
		this.checkerConfs = conf.getCheckers();
		
		
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
		
		
		checkersList = new org.eclipse.swt.widgets.List(upperBlock, SWT.SINGLE | SWT.BORDER);
		GridData listLD = new GridData(GridData.FILL_HORIZONTAL);
		listLD.heightHint = 100;
		checkersList.setLayoutData(listLD);
		
		Composite buttonBlock = new Composite(upperBlock, SWT.NONE);
		buttonBlock.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		buttonBlock.setLayoutData(new GridData(GridData.BEGINNING));
		buttonBlock.setLayout(new FillLayout(SWT.VERTICAL));
		Button addButton = new Button(buttonBlock, SWT.PUSH);

		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addNewChecker();
			}
		});
		
		Button removeButton = new Button(buttonBlock, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelectedChecker();
			}
		});
		
		
		updateCheckersList();
	}
	
	private void createDetailForm(Composite parent) {
		Composite form = new Composite(parent, SWT.NONE);
		//form.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout formLayout = new GridLayout(2, false);
		formLayout.marginWidth = 0;
		formLayout.marginTop = 30;
		form.setLayout(formLayout);
		
//		Label patternLabel = new Label(form, SWT.WRAP);
//		patternLabel.setText("Connection pattern:");
//		patternText = new Text(form, SWT.BORDER);
//		patternText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		patternText.addModifyListener(modifyListener);
		
		Label checkerNameLabel = new Label(form, SWT.NONE);
		checkerNameLabel.setText("Checker:");
		checkerNameCombo = new Combo(form, SWT.READ_ONLY);
		
		
		Label driverLabel = new Label(form, SWT.NONE);
		driverLabel.setText("Driver:");
		driverText = new Text(form, SWT.BORDER);
		driverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label urlLabel = new Label(form, SWT.NONE);
		urlLabel.setText("URL:");
		urlText = new Text(form, SWT.BORDER);
		urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label usernameLabel = new Label(form, SWT.NONE);
		usernameLabel.setText("Username:");
		usernameText = new Text(form, SWT.BORDER);
		usernameText.setLayoutData(new GridData(100, SWT.DEFAULT));
		
		Label passwordLabel = new Label(form, SWT.NONE);
		passwordLabel.setText("Password:");
		passwordText = new Text(form, SWT.BORDER);
		passwordText.setLayoutData(new GridData(100, SWT.DEFAULT));
		
		Label patternsLabel = new Label(parent, SWT.NONE);
		patternsLabel.setText("Connection patterns [EXPERIMENTAL, leave empty or consult documentation]");
		patternsText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		patternsText.setFont(JFaceResources.getTextFont());
		patternsText.setLayoutData(new RowData(SWT.DEFAULT, 100));
		
		
		// set things ready for usage
		loadCheckersInfo();
		
		checkerNameCombo.addModifyListener(modifyListener);
		driverText.addModifyListener(modifyListener);
		urlText.addModifyListener(modifyListener);
		usernameText.addModifyListener(modifyListener);
		passwordText.addModifyListener(modifyListener);
		patternsText.addModifyListener(modifyListener);
		
		
	}

	private void loadCheckersInfo() {
		checkerInfos.clear();
		checkerNameCombo.removeAll();
		checkerNameCombo.add("");
		for (CheckerInfo ci : CheckersManager.getAvailableCheckersInfo()) {
			checkerInfos.put(ci.getCheckerName(), ci);
			checkerNameCombo.add(ci.getCheckerName());
		}
	}

	@Override
	protected Label createDescriptionLabel(Composite parent) {
		Label desc = new Label(parent, SWT.WRAP);
		desc.setText("Here you can specify test databases or SQL grammars " +
				"for checking SQL statements found in your program.\n\n" +
				"If you list several checkers, then each statement " +
				"should normally pass at least one of them. " +
				"See also documentation for 'connection patterns'.");
		return desc;
	}
	

	private void updateForm() {
		int i = checkersList.getSelectionIndex();
		if (i > -1 && this.checkerConfs.size() > i) {
			CheckerConfiguration prop = this.checkerConfs.get(i);
	//		updateText(patternText, prop.getPattern());
			updateComboSelection(checkerNameCombo, prop.getCheckerName());
			updateText(driverText, prop.getDriverName());
			updateText(urlText, prop.getUrl());
			updateText(usernameText, prop.getUserName());
			updateText(passwordText, prop.getPassword());
			updateText(patternsText, prop.getPatternsAsString());
		} 
		else { // nothing is selected
			updateComboSelection(checkerNameCombo, "");
			updateText(driverText, "");
			updateText(urlText, "");
			updateText(usernameText, "");
			updateText(passwordText, "");
			updateText(patternsText, "");
		}
		
		updateEnabled();
	}
	
	private void updateEnabled() {
		CheckerInfo ci = checkerInfos.get(checkerNameCombo.getText());
		boolean usesDatabase = ci != null && ci.getUsesDatabase();
		
		driverText.setEnabled(usesDatabase);
		urlText.setEnabled(usesDatabase);
		usernameText.setEnabled(usesDatabase);
		passwordText.setEnabled(usesDatabase);
	}
	
	private void fieldChanged(Widget field) {
		
		// prepare new place, if there are no checkers yet (and nothing can be selected)
		// and users starts editing before pressing "Add"
		if (checkerConfs.size() == 0) {
			checkerConfs.add(new CheckerConfiguration("", "", "", "", "", new ArrayList<String>()));
		}
		int i = checkersList.getSelectionIndex();
		if (i == -1) {
			i = 0;
		}
		
		CheckerConfiguration prop = this.checkerConfs.get(i);
		//if (text == patternText) { prop.setPattern(text.getText()); }
		if (field == checkerNameCombo) {
			String checkerName = ((Combo)field).getText(); 
			prop.setCheckerName(checkerName);
			CheckerInfo ci = checkerInfos.get(checkerName);
			
			if (ci != null) {
				driverText.setText(ci.getDefaultDriver());
			}
			else if (checkerName != null) {
				driverText.setText("");
				urlText.setText("");
				usernameText.setText("");
				passwordText.setText("");
			}
			
			updateEnabled();
		}
		else if (field == driverText) { prop.setDriverName(((Text)field).getText()); }
		else if (field == urlText) { prop.setUrl(((Text)field).getText()); }
		else if (field == usernameText) { prop.setUserName(((Text)field).getText()); }
		else if (field == passwordText) { prop.setPassword(((Text)field).getText()); }
		else if (field == patternsText) { prop.setPatterns(((Text)field).getText()); }
		
		this.hasChanges = true;
		updateCheckersList();
	}
	
	private void updateCheckersList() {
		int selIndex = checkersList.getSelectionIndex();
		checkersList.removeSelectionListener(listSelectionListener);
		try {
			for (int i = 0; i < checkerConfs.size(); i++) {
				if (checkersList.getItemCount() < checkerConfs.size()) {
					checkersList.add("");
				}
				String label = getCheckerLabel(checkerConfs.get(i));
				if (!checkersList.getItem(i).equals(label)) {
					checkersList.setItem(i, label);
				}
			}
		
			while (checkersList.getItemCount() > checkerConfs.size()) {
				checkersList.remove(checkersList.getItemCount()-1);
			}
		} 
		finally {
			checkersList.addSelectionListener(listSelectionListener);
			
			if (selIndex > checkersList.getItemCount()-1) {
				selIndex = checkersList.getItemCount()-1;
			}
			
			if (checkersList.getSelectionIndex() != selIndex) {
				checkersList.setSelection(selIndex);
			}
		}
		
		
	}

	@Override
	protected void mergeChanges(ProjectConfiguration base) {
		base.setCheckers(this.checkerConfs);
	}
	
	private void removeSelectedChecker() {
		
		int i = checkersList.getSelectionIndex();
		if (i < 0) {
			return;
		}
		
		this.hasChanges = true;
		checkerConfs.remove(i);
		tryToSelectRow(0);
		updateCheckersList();
		updateForm();
	}
	
	private void addNewChecker() {
		CheckerConfiguration prop = new CheckerConfiguration("", "", "", "", "", new ArrayList<String>());
		checkerConfs.add(prop);
		updateCheckersList();
		tryToSelectRow(checkerConfs.size()-1);
		
	}
	
	private void tryToSelectRow(int rowNo) {
		checkersList.select(rowNo);
		updateForm();
	}
	
	private void updateComboSelection(Combo combo, String content) {
		combo.removeModifyListener(this.modifyListener); // don't want infinite loop here
		try {
			combo.setText(content);
		} finally {
			combo.addModifyListener(this.modifyListener);
		}
	}
	
	private void updateText(Text text, String content) {
		text.removeModifyListener(this.modifyListener); // don't want infinite loop here
		try {
			if (content == null) {
				text.setText("");
			}
			else {
				text.setText(content);
			}
		} finally {
			text.addModifyListener(this.modifyListener);
		}
	}
	
	private String getCheckerLabel(CheckerConfiguration conf) {
		if (conf.getCheckerName().isEmpty()) {
			return "<new checker>";
		}
		else {
			String result = conf.getCheckerName();
			if (!conf.getUrl().isEmpty()) {
				result += ", " + conf.getUrl();
				if (!conf.getUserName().isEmpty()) {
					result += ", " + conf.getUserName();
				}
			}
			return result;
		}
	}
}
