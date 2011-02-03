package com.zeroturnaround.alvor.gui.configuration;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
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
		IAdaptable element = this.getElement();
		
		// can be IProject or IJavaProject, depending on extension definition
		// I'll handle both cases, just in case
		if (element instanceof IProject) {
			return (IProject)element;
		}
		else if (element instanceof IJavaProject) {
			return ((IJavaProject)element).getProject();
		}
		else {
			throw new IllegalStateException("Unknown element");
		}
	}
	
	protected abstract void mergeChanges(ProjectConfiguration base);
	
	protected boolean saveState() {
		try {
			ProjectConfiguration conf = this.readConfiguration();
			this.mergeChanges(conf);
			ConfigurationManager.saveProjectConfiguration(conf, this.getSelectedProject());
			this.hasChanges = false;
			return true;
		} catch (Exception e) {
			GuiUtil.showErrorDialog("Problem saving configuration", e);
			LOG.exception(e);
			return false;
		}
	}
	
	@Override
	public boolean performOk() {
		if (this.hasChanges) {
			//GuiUtil.ShowInfoDialog("Please do full analysis after changing SQL checker configuration");
			// TODO, should I initiate full (or partial) reanalysis? Clean the cache?
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
