package com.zeroturnaround.alvor.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

import com.zeroturnaround.alvor.builder.AlvorBuilder;

public class CleanBuildHandler extends AbstractHandler {
	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IProject project = GuiUtil.getSelectedJavaProject();
		try {
			project.build(IncrementalProjectBuilder.CLEAN_BUILD, AlvorBuilder.BUILDER_ID, null, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		IProject project = GuiUtil.getSelectedJavaProject();
		return project != null && AlvorBuilder.getAlvorBuilder(project) != null;
	}
	
}
