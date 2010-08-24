package com.zeroturnaround.alvor.gui;

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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.zeroturnaround.alvor.cache.CacheService;
import com.zeroturnaround.alvor.checkers.AbstractStringCheckerManager;
import com.zeroturnaround.alvor.checkers.INodeDescriptor;
import com.zeroturnaround.alvor.checkers.ISQLErrorHandler;
import com.zeroturnaround.alvor.checkers.IStringNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.crawler.NodeSearchEngine;
import com.zeroturnaround.alvor.crawler.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.main.JavaElementChecker;
import com.zeroturnaround.alvor.main.OptionLoader;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;
import com.zeroturnaround.alvor.string.util.AbstractStringOptimizer;

public class CheckProjectHandler extends AbstractHandler implements ISQLErrorHandler {
	public static final String ERROR_MARKER_ID = "com.zeroturnaround.alvor.gui.sqlerror";
	public static final String WARNING_MARKER_ID = "com.zeroturnaround.alvor.gui.sqlwarning";
	public static final String HOTSPOT_MARKER_ID = "com.zeroturnaround.alvor.gui.sqlhotspot";
	public static final String UNSUPPORTED_MARKER_ID = "com.zeroturnaround.alvor.gui.unsupported";
	public static final String STRING_MARKER_ID = "com.zeroturnaround.alvor.gui.sqlstring";

	private static final ILog LOG = Logs.getLog(CheckProjectHandler.class);
	
	private JavaElementChecker projectChecker = new JavaElementChecker();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Runtime.getRuntime().gc();
		System.out.println("MEM: totalMemory() == " + Runtime.getRuntime().totalMemory());
		System.out.println("MEM: maxMemory() == " + Runtime.getRuntime().maxMemory());
		System.out.println("MEM: freeMemory() == " + Runtime.getRuntime().freeMemory());
		System.out.println("MEM: used memory == " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		
		NodeSearchEngine.clearCache();
		CacheService.getCacheService().clearAll();
		Timer timer = new Timer();
		timer.start("TIMER: whole process");
		assert LOG.message("CheckProjectHandler.execute");
		List<IJavaElement> selectedJavaElements = GuiUtil.getSelectedJavaElements();
		for (IJavaElement element : selectedJavaElements) {
			try {
				performCheck(element, new IJavaElement[] {element});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		timer.printTime();
		return null; // Must be null
	}

	/**
	 * NB! Before calling this you should take care that NodeSearchEngine's ASTCache doesn't
	 * contain old stuff (either clear it completely or remove updated AST-s)
	 */
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
	
	private void cleanMarkers(IJavaElement[] scope) {
		try {
			for (IJavaElement element : scope) {
				element.getResource().deleteMarkers(ERROR_MARKER_ID, true, IResource.DEPTH_INFINITE);
				element.getResource().deleteMarkers(WARNING_MARKER_ID, true, IResource.DEPTH_INFINITE);
				element.getResource().deleteMarkers(HOTSPOT_MARKER_ID, true, IResource.DEPTH_INFINITE);
				element.getResource().deleteMarkers(UNSUPPORTED_MARKER_ID, true, IResource.DEPTH_INFINITE);
				element.getResource().deleteMarkers(STRING_MARKER_ID, true, IResource.DEPTH_INFINITE);
			}
		} catch (Exception e) {
			LOG.exception(e);
		}
	}
	
	private static void createMarker(String message, String markerType, IPosition pos) {
		Map<String, Comparable<?>> map = new HashMap<String, Comparable<?>>();
	
		if (!HOTSPOT_MARKER_ID.equals(markerType)) {
			int severity = markerType.equals(WARNING_MARKER_ID) ? 
					IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR;
			map.put(IMarker.SEVERITY, new Integer(severity));
		}
		
		createMarker(message, markerType, pos, map);
	}

	public static void createMarker(String message, String markerType,
			IPosition pos, Map<String, Comparable<?>> map) {
		if (pos.getPath().equals("__dummy__")) {
			return;
		}
		
		
		if (map == null) {
			map = new HashMap<String, Comparable<?>>();
		}
		
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(pos.getPath()));
		int charStart = pos.getStart();
		int charEnd = charStart + pos.getLength();

//		LOG.message("creating marker: " + message + ", file=" + file
//				+ ", charStart=" + charStart + ", charEnd=" + charEnd + ", type=" + markerType);
		
		String finalMessage = message;
		if (message.length() > 500) {
			finalMessage = message.substring(0, 500) + "...";
		}
		if (markerType.equals(WARNING_MARKER_ID)) {
			finalMessage = "Unsupported SQL construction: " + message;
		}
		else if (markerType.equals(ERROR_MARKER_ID)) {
			finalMessage = "SQL checker: " + message;
		}
		
		MarkerUtilities.setMessage(map, finalMessage);
		MarkerUtilities.setCharStart(map, charStart);
		MarkerUtilities.setCharEnd(map, charEnd);
		map.put(IMarker.LOCATION, file.getFullPath().toString());
		try {
			MarkerUtilities.createMarker(file, map, markerType);
		} catch (Exception e) {
			LOG.exception(e);
		}
	}

	private void markHotspots(List<INodeDescriptor> hotspots) {
		for (INodeDescriptor hotspot : hotspots) {
			String message;
			String markerId;
			if (hotspot instanceof IStringNodeDescriptor) {
				IStringNodeDescriptor snd = (IStringNodeDescriptor) hotspot;
				IAbstractString abstractValue = snd.getAbstractValue();
				message = "Abstract string: " + CommonNotationRenderer.render(AbstractStringOptimizer.optimize(abstractValue));
				if (message.length() > Character.MAX_VALUE) {
					message = message.substring(0, Character.MAX_VALUE - 3) + "...";
				}
				markerId = HOTSPOT_MARKER_ID;
//				markConstants(abstractValue);
			} else if (hotspot instanceof UnsupportedNodeDescriptor) {
				UnsupportedNodeDescriptor und = (UnsupportedNodeDescriptor) hotspot;
				message = "Unsupported construction: " + und.getProblemMessage();
				markerId = UNSUPPORTED_MARKER_ID;
			} else {
				throw new IllegalArgumentException(hotspot + "");
			}
			createMarker(
					message, 
					markerId, 
					hotspot.getPosition());
		}		
	}

	/*
	 * This method makes things slow on big projects, although on small ones the markers look nice
	 */
	@SuppressWarnings("unused")
	private void markConstants(IAbstractString abstractValue) {
		IAbstractStringVisitor<Void, Void> visitor = new IAbstractStringVisitor<Void, Void>() {

			@Override
			public Void visitStringCharacterSet(
					StringCharacterSet characterSet, Void data) {
				createMarker("", STRING_MARKER_ID, characterSet.getPosition());
				return null;
			}

			@Override
			public Void visitStringChoice(StringChoice stringChoice, Void data) {
				for (IAbstractString s : stringChoice.getItems()) {
					s.accept(this, null);
				}
				return null;
			}

			@Override
			public Void visitStringConstant(StringConstant stringConstant,
					Void data) {
				createMarker("", STRING_MARKER_ID, stringConstant.getPosition());
				return null;
			}

			@Override
			public Void visitStringParameter(StringParameter stringParameter,
					Void data) {
				return null;
			}

			@Override
			public Void visitStringRepetition(
					StringRepetition stringRepetition, Void data) {
				stringRepetition.getBody().accept(this, null);
				return null;
			}

			@Override
			public Void visitStringSequence(StringSequence stringSequence,
					Void data) {
				for (IAbstractString s : stringSequence.getItems()) {
					s.accept(this, null);
				}
				return null;
			}
		};
		abstractValue.accept(visitor, null);
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
