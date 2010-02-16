package ee.stacc.productivity.edsl.gui;

import static ee.stacc.productivity.edsl.gui.Logger.LOG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

import ee.stacc.productivity.edsl.checkers.AbstractStringCheckerManager;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.main.JavaElementChecker;
import ee.stacc.productivity.edsl.main.OptionLoader;

public class CheckProjectHandler extends AbstractHandler implements ISQLErrorHandler {
	public static final String ERROR_MARKER_ID = "ee.stacc.productivity.edsl.gui.sqlerror";
	public static final String WARNING_MARKER_ID = "ee.stacc.productivity.edsl.gui.sqlwarning";
	public static final String HOTSPOT_MARKER_ID = "ee.stacc.productivity.edsl.gui.sqlhotspot";
	
	private JavaElementChecker projectChecker = new JavaElementChecker();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LOG.message("CheckProjectHandler.execute");
		IJavaElement selectedJavaElement = getSelectedJavaElement();
		cleanMarkers(selectedJavaElement);
		try {
			Map<String, Object> options = OptionLoader.getElementSqlCheckerProperties(selectedJavaElement);
			List<IStringNodeDescriptor> hotspots = projectChecker.findHotspots(selectedJavaElement, options);
			markHotspots(hotspots);
			
			projectChecker.checkHotspots(hotspots, this,
					AbstractStringCheckerManager.INSTANCE.getCheckers(),
					options
			);
		} catch (Throwable e) {
			LOG.exception(e);
			throw new ExecutionException("Error during checking: " + e.getMessage(), e);
		}
		return null; // Must be null
	}
	
	/*
	 * @return an IJavaElement currently selected in the workbench
	 */
	private IJavaElement getSelectedJavaElement() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection structSel = (StructuredSelection) selection;
			Object firstElement = structSel.getFirstElement();
			if (firstElement instanceof IJavaElement) {
				return (IJavaElement) firstElement;
			} else if (firstElement instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) firstElement;
				Object adapter = adaptable.getAdapter(IJavaElement.class);
				return (IJavaElement) adapter;
			}
		}
		throw new IllegalStateException("No Java element selected");
	}

	private void cleanMarkers(IJavaElement scope) {
		try {
			scope.getResource().deleteMarkers(ERROR_MARKER_ID, true, IResource.DEPTH_INFINITE);
			scope.getResource().deleteMarkers(WARNING_MARKER_ID, true, IResource.DEPTH_INFINITE);
			scope.getResource().deleteMarkers(HOTSPOT_MARKER_ID, true, IResource.DEPTH_INFINITE);
		} catch (Exception e) {
			LOG.exception(e);
		}
	}
	
	private void createMarker(String message, String markerType, IFile file, int charStart, int charEnd) {
		LOG.message("creating marker: " + message + ", file=" + file
				+ ", charStart=" + charStart + ", charEnd=" + charEnd);
		
		Map<String, Comparable<?>> map = new HashMap<String, Comparable<?>>();
		MarkerUtilities.setMessage(map, message);
		map.put(IMarker.LOCATION, file.getFullPath().toString());
		map.put(IMarker.CHAR_START, charStart);
		map.put(IMarker.CHAR_END, charEnd);
	
		if (!HOTSPOT_MARKER_ID.equals(markerType)) {
			int severity = markerType.equals(WARNING_MARKER_ID) ? 
					IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR;
			map.put(IMarker.SEVERITY, new Integer(severity));
		}
		
		try {
			MarkerUtilities.createMarker(file, map, markerType);
		} catch (Exception e) {
			LOG.exception(e);
		}
	}

	private void markHotspots(List<IStringNodeDescriptor> hotspots) {
		for (IStringNodeDescriptor hotspot : hotspots) {
			createMarker("Abstract string: " + hotspot.getAbstractValue(), HOTSPOT_MARKER_ID, 
					hotspot.getFile(), 
					hotspot.getCharStart(), 
					hotspot.getCharLength() + hotspot.getCharStart());
		}		
	}

	@Override
	public void handleSQLError(String message, IStringNodeDescriptor descriptor) {
		createMarker(message, ERROR_MARKER_ID, descriptor.getFile(), descriptor.getCharStart(), 
				descriptor.getCharStart() + descriptor.getCharLength());		
	}
}
