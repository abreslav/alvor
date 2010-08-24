package ee.stacc.productivity.edsl.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.crawler.NodeSearchEngine;

public class ClearCacheHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		NodeSearchEngine.clearCache();
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection structSel = (StructuredSelection) selection;
			Object firstElement = structSel.getFirstElement();
			if (firstElement instanceof IProject) {
				IProject project = (IProject) firstElement;
			}
			CacheService.getCacheService().clearAll();
		}
		return null;
	}


}
