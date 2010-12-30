package com.zeroturnaround.alvor.gui.configuration;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.configuration.ConfigurationManager;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.gui.GuiUtil;

public abstract class CommonPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	protected boolean hasChanges = false;
	protected final static ILog LOG = Logs.getLog(CommonPropertyPage.class);

	
	protected ProjectConfiguration readConfiguration() {
		return ConfigurationManager.readProjectConfiguration(this.getSelectedProject().getProject(), true);
	}
	
	protected IProject getSelectedProject() {
		return (IProject)this.getElement();
	}
	
	protected abstract void mergeChanges(ProjectConfiguration base);
	
	protected boolean saveState() {
		try {
			ProjectConfiguration conf = this.readConfiguration();
			this.mergeChanges(conf);
			ConfigurationManager.saveToProjectConfigurationFile(conf, this.getSelectedProject());
			this.hasChanges = false;
			return true;
		} catch (Exception e) {
			GuiUtil.ShowErrorDialog("Problem saving configuration", e);
			LOG.exception(e);
			return false;
		}
	}
	
	@Override
	public boolean performOk() {
		if (this.hasChanges) {
			GuiUtil.ShowInfoDialog("Please do full analysis after changing SQL checker configuration");
			return this.saveState();
		}
		else {
			return true;
		}
	}
	
	@Override
	protected void performApply() {
		this.saveState();
	}
}