package com.zeroturnaround.alvor.gui;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.crawler.NodeSearchEngine;
import com.zeroturnaround.alvor.main.ResultSetChecker;



public class CheckResultSetsHandler extends AbstractHandler {
	private static final ILog LOG = Logs.getLog(CheckResultSetsHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IJavaElement> selectedJavaElements = GuiUtil.getSelectedJavaElements();
		try {
			for (IJavaElement element : selectedJavaElements) {
				ResultSetChecker.checkUsages(new IJavaElement[] {element});
			}
		} catch (Exception e) {
			LOG.exception(e);
		}
		return null;
	}
}
