package com.zeroturnaround.alvor.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import com.zeroturnaround.alvor.cache.CacheProvider;

public class ClearCacheHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IProject project = GuiUtil.getSelectedJavaProject();
		CacheProvider.getCache(project.getName()).clearProject();
		GuiChecker.deleteAlvorMarkers(ResourcesPlugin.getWorkspace().getRoot());
		return null;
	}

}
