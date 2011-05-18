package com.googlecode.alvor.gui.configuration;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.googlecode.alvor.builder.AlvorBuilder;
import com.googlecode.alvor.builder.AlvorNature;
import com.googlecode.alvor.configuration.ProjectConfiguration;

public class MainPropertyPage extends CommonPropertyPage {
	private Button natureCheckbox; 
	private Combo effortCombo;
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		
		
		natureCheckbox = new Button(composite, SWT.CHECK);
		natureCheckbox.setText("Plug Alvor to project's build process");
		// initialize check-box
		natureCheckbox.setSelection(this.builderAndNatureAreInstalled());
		natureCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hasChanges = true;
			}
		});
		
		Label natureLabel = new Label(composite, SWT.WRAP);
		natureLabel.setText("This gives automatic incremental checking on each save, " +
				"and full checking on 'Clean'.\n" +
				"NB! First checking can take several minutes for big projects.\n\n" +
				"Altenatively, you can always right-click your project in Package Explorer\n" +
				"and select 'Check SQL' menu item.\n\n");
		
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
	    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// effort
	    ProjectConfiguration conf = this.readConfiguration();
	    GridLayout effortLayout = new GridLayout(2, false);
		effortLayout.marginTop = 10;
		Composite effortComposite = new Composite(composite, SWT.NONE);
		effortComposite.setLayout(effortLayout);
		Label effortLabel = new Label(effortComposite, SWT.NONE);
		effortLabel.setText("Analysis mode: ");
		this.effortCombo = new Combo(effortComposite, SWT.READ_ONLY);
		effortCombo.add("fastest (skips complex cases of SQL construction)");
		effortCombo.add("normal (recommended for most projects)");
		effortCombo.add("thorough (may be too slow in some cases)");
		if (conf.getEffortLevel() == 1) {
			effortCombo.select(0);
		}
		else if (conf.getEffortLevel() == 3) {
			effortCombo.select(2);
		}
		else {
			effortCombo.select(1);
		}
		effortCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				hasChanges = true;
			}
		});
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
		if (natureCheckbox.getSelection() != this.builderAndNatureAreInstalled()) {
			AlvorNature.toggleNature(getSelectedProject());
			// build can start at this point
		}
		return super.saveState();
	}
	
	@Override
	protected void mergeChanges(ProjectConfiguration base) {
		base.setProperty("effortLevel", String.valueOf(this.effortCombo.getSelectionIndex()+1));
	}
	
	private boolean builderAndNatureAreInstalled() {
		try {
			boolean natureIsInstalled = this.getSelectedProject().hasNature(AlvorNature.NATURE_ID);
			boolean builderIsInstalled = false;
			ICommand[] commands = this.getSelectedProject().getDescription().getBuildSpec();
			for (int i = 0; i < commands.length; ++i) {
				if (commands[i].getBuilderName().equals(AlvorBuilder.BUILDER_ID)) {
					builderIsInstalled = true;
					break;
				}
			}
		
			
			if (natureIsInstalled && !builderIsInstalled
					|| !natureIsInstalled && builderIsInstalled) {
				LOG.error("Builder and Nature disagree", null);
			}
			return natureIsInstalled && builderIsInstalled;
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
}