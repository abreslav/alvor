package com.zeroturnaround.alvor.gui.configuration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.zeroturnaround.alvor.builder.AlvorNature;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;

public class MainPropertyPage extends CommonPropertyPage {
	private Button natureCheckbox; 
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		
		
		natureCheckbox = new Button(composite, SWT.CHECK);
		natureCheckbox.setText("Plug Alvor to project's build process");
		// initialize check-box
		try {
			natureCheckbox.setSelection(this.getSelectedProject().hasNature(AlvorNature.NATURE_ID));
		} catch (CoreException e) {
			e.printStackTrace();
		}
		natureCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hasChanges = true;
			}
		});
		
		Label natureLabel = new Label(composite, SWT.WRAP);
		natureLabel.setText("This gives automatic incremental checking on each save, " +
				"and full checking on 'Clean'.\n" +
				"NB! First checking can take several minutes for big projects.\n\n\n" +
				"Altenatively, you can always use 'Project' -> 'Check SQL' menu item.");
		
		return composite;
	}
	
	
//	@Override
//	protected Label createDescriptionLabel(Composite parent) {
//		Label desc = new Label(parent, SWT.WRAP);
//		desc.setText("Alvor can be used with any Java project just by selecting " +
//				"'Project' -> 'Check SQL' menu item. Here you can ");
//		return desc;
//	}

	
	@Override
	public boolean saveState() {
		try {
			if (natureCheckbox.getSelection() != getSelectedProject().hasNature(AlvorNature.NATURE_ID)) {
				AlvorNature.toggleNature(getSelectedProject());
				// build can start at this point
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return super.saveState();
	}
	
	@Override
	protected void mergeChanges(ProjectConfiguration base) {
		// this page doesn't affect configuration in ProjectConfiguration
	}
}