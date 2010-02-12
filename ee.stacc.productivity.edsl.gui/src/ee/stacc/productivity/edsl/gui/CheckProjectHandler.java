package ee.stacc.productivity.edsl.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

import ee.stacc.productivity.edsl.checkers.AbstractStringCheckerManager;
import ee.stacc.productivity.edsl.checkers.ISQLErrorHandler;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.main.SQLUsageChecker;


public class CheckProjectHandler extends AbstractHandler implements ISQLErrorHandler {
	public static final String ERROR_MARKER_ID = "ee.stacc.productivity.edsl.gui.sqlerror";
	public static final String WARNING_MARKER_ID = "ee.stacc.productivity.edsl.gui.sqlwarning";
	public static final String HOTSPOT_MARKER_ID = "ee.stacc.productivity.edsl.gui.sqlhotspot";
	
	private SQLUsageChecker projectChecker = new SQLUsageChecker();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("CheckProjectHandler.execute");
		IJavaElement selectedJavaElement = getSelectedJavaElement();
		cleanMarkers(selectedJavaElement);
		try {
			List<IStringNodeDescriptor> hotspots = projectChecker.findHotspots(selectedJavaElement);
			markHotspots(hotspots);
			
			projectChecker.checkJavaElement(hotspots, this,
					AbstractStringCheckerManager.INSTANCE.getCheckers()
				//StaticSQLChecker.SQL_LEXICAL_CHECKER,
				//StaticSQLChecker.SQL_SYNTAX_CHECKER,  
//				new DynamicSQLChecker(getElementSqlCheckerProperties(selectedJavaElement))
			);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new ExecutionException("Error during checking: " + e.getMessage(), e);
		}
		return null; // Must be null
	}
	
	private Properties getElementSqlCheckerProperties(IJavaElement element)
			throws FileNotFoundException, IOException {
		IJavaProject project = element.getJavaProject();
		File propsFile = project.getResource().getLocation()
			.append("/sqlchecker.properites").toFile();
		System.out.println("PROPS_FILE: " + propsFile);
		FileInputStream in = new FileInputStream(propsFile);
		Properties props = new Properties();
		props.load(in);
		return props;
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
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	private void createMarker(String message, String markerType, IFile file, int charStart, int charEnd) {
		System.out.println("creating marker: " + message + ", file=" + file
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
			System.err.println("Error creating marker: " + e.getMessage());
		}
	}

	private void markHotspots(List<IStringNodeDescriptor> hotspots) {
		for (IStringNodeDescriptor hotspot : hotspots) {
			createMarker("Hotspot: " + hotspot.getAbstractValue(), HOTSPOT_MARKER_ID, 
					hotspot.getFile(), 
					hotspot.getCharStart(), 
					hotspot.getCharLength() + hotspot.getCharStart());
		}		
	}

	@Override
	public void handleSQLError(String message, IFile file, int startPosition,
			int length) {
		//System.err.println(e.getMessage());
		createMarker(message, ERROR_MARKER_ID, file, startPosition, 
				startPosition + length);		
	}
}
