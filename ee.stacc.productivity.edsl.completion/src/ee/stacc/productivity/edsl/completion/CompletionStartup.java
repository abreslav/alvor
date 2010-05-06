package ee.stacc.productivity.edsl.completion;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.crawler.PositionUtil;
import ee.stacc.productivity.edsl.gui.CheckProjectHandler;
import ee.stacc.productivity.edsl.string.IPosition;

public class CompletionStartup implements IStartup {
	
	private static final String TOKEN_MARKER_ID = "ee.stacc.productivity.edsl.completion.token";

	private static final ILog LOG = Logs.getLog(CompletionStartup.class); 

	private static final ISelectionListener LISTENER = new ISelectionListener() {
		
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!JavaUI.ID_CU_EDITOR.equals(part.getSite().getId())) {
				return;
			}
			
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				int offset = textSelection.getOffset();
				if (part instanceof IEditorPart) {
					IEditorPart editorPart = (IEditorPart) part;
					
					IEditorInput editorInput = editorPart.getEditorInput();
					if (editorInput instanceof IFileEditorInput) {
						IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
						IFile file = fileEditorInput.getFile();
						
						try {
							file.deleteMarkers(TOKEN_MARKER_ID, true, IResource.DEPTH_INFINITE);
						} catch (CoreException e) {
							// Nothing
						}

						String fileString = PositionUtil.getFileString(file);
						
						Collection<IPosition> positions = TokenLocator.INSTANCE.locateToken(fileString, offset);
						for (IPosition position : positions) {
							CheckProjectHandler.createMarker("asdas", TOKEN_MARKER_ID, position, null);
						}
						return;
					}
				}
			}
			LOG.error("Failed to process the selection");
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
