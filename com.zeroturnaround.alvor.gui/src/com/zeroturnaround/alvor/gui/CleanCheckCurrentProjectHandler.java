package com.zeroturnaround.alvor.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class CleanCheckCurrentProjectHandler extends AbstractHandler {
	GuiChecker checker = new GuiChecker();
	private static final ILog LOG = Logs.getLog(CleanCheckCurrentProjectHandler.class);
	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IJavaElement element = GuiUtil.getCurrentJavaProject();
			LOG.message("Checking project: " + element.getElementName());
			checker.performCleanCheck(element, new IJavaElement[] {element});
		} 
		catch (Exception e) {
			LOG.exception(e);
		}
		
		return null;
	}

}
