package com.zeroturnaround.alvor.gui.configuration;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MainPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		
		Label label = new Label(parent, SWT.WRAP);
		label.setText("\nAlvor is configured project-wise.\n" +
				"Right-click on project and select 'Properties' -> 'Alvor SQL Checker'");
		return null;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}