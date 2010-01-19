package ee.stacc.productivity.edsl.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ee.stacc.productivity.edsl.main.SQLUsageChecker;


public class CheckProjectHandler extends AbstractHandler {
	SQLUsageChecker projectChecker = new SQLUsageChecker();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("CheckProjectHandler.execute");
		
		//projectChecker.checkProject(getCurrentProject());
		return null;
	}
	
	IJavaProject getCurrentProject() {
		ITypeRoot root = JavaUI.getEditorInputTypeRoot(getActiveEditor().getEditorInput());
		return root.getJavaProject();
	}
	
	IEditorPart getActiveEditor() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				return page.getActiveEditor();
			}
		}
		return null;
	}
	
}
