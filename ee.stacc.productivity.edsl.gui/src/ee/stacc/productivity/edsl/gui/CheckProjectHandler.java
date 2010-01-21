package ee.stacc.productivity.edsl.gui;

import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

import ee.stacc.productivity.edsl.main.SQLUsageChecker;


public class CheckProjectHandler extends AbstractHandler {
	public static final String ERROR_MARKER_ID = "EclipseSQLPlugin.sqlerror";
	public static final String WARNING_MARKER_ID = "EclipseSQLPlugin.sqlwarning";
	SQLUsageChecker projectChecker = new SQLUsageChecker();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("CheckProjectHandler.execute");
		projectChecker.checkProject(getCurrentProject());
		return null;
	}
	
	IJavaProject getCurrentProject() {
		ITypeRoot root = JavaUI.getEditorInputTypeRoot(getActiveEditor().getEditorInput());
		return root.getJavaProject();
	}
	
	private void cleanMarkers(IJavaElement scope) {
		try {
			scope.getResource().deleteMarkers(ERROR_MARKER_ID, true, IResource.DEPTH_INFINITE);
			scope.getResource().deleteMarkers(WARNING_MARKER_ID, true, IResource.DEPTH_INFINITE);
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
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
	
	void createMarker(String message, String markerType, IFile file, int charStart, int charEnd) {
		
		@SuppressWarnings("unchecked")
		HashMap<String, Comparable> map = new HashMap<String, Comparable>();
		MarkerUtilities.setMessage(map, message);
		map.put(IMarker.LOCATION, file.getFullPath().toString());
		map.put(IMarker.CHAR_START, charStart);
		map.put(IMarker.CHAR_END, charEnd);
	
		
		int severity = markerType.equals(WARNING_MARKER_ID) ? 
				IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR;
		map.put(IMarker.SEVERITY, new Integer(severity));
		
		try {
			MarkerUtilities.createMarker(file, map, markerType);
		} catch (Exception e) {
			System.err.println("Error creating marker: " + e.getMessage());
		}
	}
	
}
