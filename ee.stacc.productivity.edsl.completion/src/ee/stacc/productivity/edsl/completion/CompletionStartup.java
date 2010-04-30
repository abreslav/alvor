package ee.stacc.productivity.edsl.completion;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class CompletionStartup implements IStartup {

	private static final ISelectionListener LISTENER = new ISelectionListener() {
		
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!JavaUI.ID_CU_EDITOR.equals(part.getSite().getId())) {
				return;
			}
			System.out.println("a");
			
		}
	};

	@Override
	public void earlyStartup() {
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			addPostSelectionListener(window);
		}
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
			
			@Override
			public void windowOpened(IWorkbenchWindow window) {
				addPostSelectionListener(window);
			}
			
			@Override
			public void windowClosed(IWorkbenchWindow window) {
				window.getSelectionService().removePostSelectionListener(LISTENER);
			}
			
			@Override
			public void windowDeactivated(IWorkbenchWindow window) {
			}
			
			@Override
			public void windowActivated(IWorkbenchWindow window) {
			}
		});
	}

	private void addPostSelectionListener(IWorkbenchWindow window) {
		window.getSelectionService().addPostSelectionListener(LISTENER);
	}
}
