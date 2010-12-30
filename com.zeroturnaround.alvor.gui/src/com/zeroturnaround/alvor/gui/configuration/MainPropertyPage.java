package com.zeroturnaround.alvor.gui.configuration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

public class MainPropertyPage extends CommonPropertyPage {

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		
		Button autoChecking = new Button(composite, SWT.CHECK);
		autoChecking.setText("Enable automatic incremental checking for '"
				+ this.getElement().toString() + "'");

		return composite;
	}
	
	
	@Override
	protected Label createDescriptionLabel(Composite parent) {
		Label desc = new Label(parent, SWT.WRAP);
		desc.setText("Here you can specify blaa, blaa\n\nSecond paragraph");
		return desc;
	}


	@Override
	protected void mergeChanges(ProjectConfiguration base) {
	}
}