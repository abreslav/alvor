package com.zeroturnaround.alvor.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import com.zeroturnaround.alvor.builder.AlvorBuilder;
import com.zeroturnaround.alvor.cache.CacheProvider;

public class ManualClearHandler extends AbstractHandler {

	/**
		// this should be called only when AlvorBuilder is not enabled
		// otherwise builder state doesn't correspond to cache's state
		// (If you find way to call builder's clean so that it doesn't 
		// start auto_build after that, then you can do it here)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		final IProject project = GuiUtil.getSelectedJavaProject();
		CacheProvider.getCache(project.getName()).clearProject();
		GuiChecker.deleteAlvorMarkers(ResourcesPlugin.getWorkspace().getRoot());
		return null;
	}

	@Override
	public boolean isEnabled() {
		IProject project = GuiUtil.getSelectedJavaProject();
		return project != null && AlvorBuilder.getAlvorBuilder(project) == null;
	}
}
