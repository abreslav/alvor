package ee.stacc.productivity.edsl.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

import ee.stacc.productivity.edsl.checkers.AbstractStringCheckerManager;
import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.main.JavaElementChecker;
import ee.stacc.productivity.edsl.main.OptionLoader;
import ee.stacc.productivity.edsl.string.IPosition;

public class CheckProjectHandler extends AbstractHandler implements ISQLErrorHandler {
	public static final String ERROR_MARKER_ID = "ee.stacc.productivity.edsl.gui.sqlerror";
	public static final String WARNING_MARKER_ID = "ee.stacc.productivity.edsl.gui.sqlwarning";
	public static final String HOTSPOT_MARKER_ID = "ee.stacc.productivity.edsl.gui.sqlhotspot";

	private static final ILog LOG = Logs.getLog(CheckProjectHandler.class);
	
	private JavaElementChecker projectChecker = new JavaElementChecker();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LOG.message("CheckProjectHandler.execute");
		IJavaElement selectedJavaElement = getSelectedJavaElement();
		performCheck(selectedJavaElement, new IJavaElement[] {selectedJavaElement});
		return null; // Must be null
	}

	public void performCheck(IJavaElement optionsFrom, IJavaElement[] scope)
			throws ExecutionException {
		cleanMarkers(scope);
		try {
			Map<String, Object> options = OptionLoader.getElementSqlCheckerProperties(optionsFrom);
			List<INodeDescriptor> hotspots = projectChecker.findHotspots(scope, options);
			markHotspots(hotspots);
			
			projectChecker.processHotspots(hotspots, this,
					AbstractStringCheckerManager.INSTANCE.getCheckers(),
					options
			);
		} catch (Throwable e) {
			LOG.exception(e);
			throw new ExecutionException("Error during checking: " + e.getMessage(), e);
		}
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

	private void cleanMarkers(IJavaElement[] scope) {
		try {
			for (IJavaElement element : scope) {
				element.getResource().deleteMarkers(ERROR_MARKER_ID, true, IResource.DEPTH_INFINITE);
				element.getResource().deleteMarkers(WARNING_MARKER_ID, true, IResource.DEPTH_INFINITE);
				element.getResource().deleteMarkers(HOTSPOT_MARKER_ID, true, IResource.DEPTH_INFINITE);
			}
		} catch (Exception e) {
			LOG.exception(e);
		}
	}
	
	private void createMarker(String message, String markerType, IPosition pos) {
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(pos.getPath()));
		int charStart = pos.getStart();
		int charEnd = charStart + pos.getLength();
		
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

	private void markHotspots(List<INodeDescriptor> hotspots) {
		for (INodeDescriptor hotspot : hotspots) {
			createMarker(
					(hotspot instanceof IStringNodeDescriptor) ?
							("Abstract string: " + ((IStringNodeDescriptor)hotspot).getAbstractValue())
							: "Unsupported construction", 
					HOTSPOT_MARKER_ID, 
					hotspot.getPosition());
		}		
	}

	@Override
	public void handleSQLError(String message, IPosition position) {
		createMarker(message, ERROR_MARKER_ID, position);		
	}

	@Override
	public void handleSQLWarning(String message, IPosition position) {
		createMarker(message, WARNING_MARKER_ID, position);		
	}
}
