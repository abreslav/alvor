package com.googlecode.alvor.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.googlecode.alvor.cache.Cache;
import com.googlecode.alvor.cache.CacheProvider;
import com.googlecode.alvor.checkers.CheckerException;
import com.googlecode.alvor.checkers.FrontChecker;
import com.googlecode.alvor.checkers.HotspotCheckingReport;
import com.googlecode.alvor.checkers.HotspotProblem;
import com.googlecode.alvor.common.HotspotDescriptor;
import com.googlecode.alvor.common.PositionUtil;
import com.googlecode.alvor.common.ProgressUtil;
import com.googlecode.alvor.common.StringHotspotDescriptor;
import com.googlecode.alvor.common.UnsupportedHotspotDescriptor;
import com.googlecode.alvor.common.WorkspaceUtil;
import com.googlecode.alvor.common.logging.ILog;
import com.googlecode.alvor.common.logging.Logs;
import com.googlecode.alvor.configuration.ConfigurationManager;
import com.googlecode.alvor.configuration.ProjectConfiguration;
import com.googlecode.alvor.crawler.StringCollector;
import com.googlecode.alvor.string.DummyPosition;
import com.googlecode.alvor.string.IPosition;
import com.googlecode.alvor.string.Position;
import com.googlecode.alvor.string.util.AbstractStringOptimizer;

public class GuiChecker {
	public static GuiChecker INSTANCE = new GuiChecker();
	private static final ILog LOG = Logs.getLog(GuiChecker.class);
	private static final int MAX_MARKER_MESSAGE_LENGTH = 500;
	private static final String CHILDREN_ATT_NAME = "children";
	private FrontChecker checker = new FrontChecker();
	//private Cache cache = CacheProvider.getCache();
	
	private GuiChecker() {}
	
	public void cleanUpdateProjectMarkers(IProject project, IProgressMonitor monitor) {
		try {
			CacheProvider.tryDeleteCache(project.getName());
			checker.resetCheckers();
			ProgressUtil.beginTask(monitor, "Full SQL check for " + project.getName(), 100);
			CacheProvider.getCache(project.getName()).clearProject();
			deleteAlvorMarkers(project);
			ProgressUtil.worked(monitor, 3);
			updateProjectMarkers(project, ProgressUtil.subMonitor(monitor, 97));
		}
		finally {
			ProgressUtil.done(monitor);
		}
	}
	
	public void updateProjectMarkers(IProject project, IProgressMonitor monitor) {
		// assumes that markers of invalidated files are already deleted
		// TODO clear project markers (about checking exceptions)
		ProgressUtil.beginTask(monitor, "Full SQL check for " + project.getName(), 100);
		Cache cache = CacheProvider.getCache(project.getName());
		try {
			StringCollector.updateProjectCache(project, ProgressUtil.subMonitor(monitor, 90));
			
			ProgressUtil.checkAbort(monitor);
			ProjectConfiguration conf = ConfigurationManager.readProjectConfiguration(project, true);
			Collection<HotspotDescriptor> hotspots = 
				cache.getPrimaryHotspots(true);
			
			ProgressUtil.checkAbort(monitor);
			createMarkersForHotspots(hotspots, conf, project, ProgressUtil.subMonitor(monitor, 10));
		} 
		finally {
			ProgressUtil.done(monitor);
		}
	}
	
	private void createMarkersForHotspots(Collection<HotspotDescriptor> hotspots, 
			ProjectConfiguration conf, IProject project, IProgressMonitor monitor) {
		
		ProgressUtil.beginTask(monitor, "Checking strings", hotspots.size());
		try {
			for (HotspotDescriptor hotspot : hotspots) {
				try {
					ProgressUtil.checkAbort(monitor);
					createMarkersForHotspot(hotspot, conf, project);
					CacheProvider.getCache(project.getName()).markHotspotAsChecked(hotspot);
					ProgressUtil.worked(monitor, 1);
				}
				catch (CheckerException e) {
					createMarker("Checker exception: " + e.getMessage(), AlvorGuiPlugin.ERROR_MARKER_ID,
							IMarker.SEVERITY_ERROR, e.getPosition(), null, project);
					break;
				}
				catch (Exception e) {
					LOG.exception(e);
				}
			}
		} 
		finally {
			ProgressUtil.done(monitor);
		}
	}
	
	private IMarker createMarkersForHotspot(HotspotDescriptor hotspot, ProjectConfiguration conf, 
			IProject project) throws CheckerException {
		
		removeOldMarkers(hotspot);
		
		IMarker hotspotMarker;
		
		if (hotspot instanceof UnsupportedHotspotDescriptor) {
			UnsupportedHotspotDescriptor uh = (UnsupportedHotspotDescriptor)hotspot;
			String msg = "Unsupported SQL construction: " + uh.getProblemMessage(); 
			if (uh.getErrorPosition() != null && !uh.getPosition().equals(uh.getErrorPosition())) {
				msg += " at: " + PositionUtil.getLineString(uh.getErrorPosition());
			}
			hotspotMarker = createMarker(msg, AlvorGuiPlugin.UNSUPPORTED_MARKER_ID, 
					IMarker.SEVERITY_INFO, hotspot.getPosition(), null, project);
		}
		
		else {
			assert hotspot instanceof StringHotspotDescriptor;
			StringHotspotDescriptor sh = (StringHotspotDescriptor)hotspot;
			
			// do checking
			HotspotCheckingReport report = checker.checkAbstractString(sh, 
					project.getName(), conf);
			
			// create hotspot marker
			StringBuilder msgBuilder = new StringBuilder();
			if (!report.getPassedCheckers().isEmpty()) {
				msgBuilder.append("Passed: ");
				for (String checkerName : report.getPassedCheckers()) {
					msgBuilder.append(checkerName);
					msgBuilder.append(", ");
				}
			}
			msgBuilder.append("Value: " + CommonNotationRenderer.render
				(AbstractStringOptimizer.optimize(sh.getAbstractValue())));
			
			String hotspotMessage;
			if (msgBuilder.length() > MAX_MARKER_MESSAGE_LENGTH) {
				hotspotMessage = msgBuilder.substring(0, MAX_MARKER_MESSAGE_LENGTH - 3) + "...";
			}
			else {
				hotspotMessage = msgBuilder.toString();
			}
			
			hotspotMarker = createMarker(hotspotMessage, AlvorGuiPlugin.HOTSPOT_MARKER_ID, 
					IMarker.SEVERITY_INFO, sh.getPosition(), null, project);

			// create problem markers if there are no passed checkers
			if (report.getPassedCheckers().isEmpty()) {
				// TODO better approach needed here, trying multiple checkers is not so good solution.
				// Normally there should be only one matching checker for each hotspot
				// or several checkers that must ALL pass.
				
				Collection<HotspotProblem> problems = report.getProblems();
				for (HotspotProblem problem : problems) { 
					createCheckingMarker(problem, hotspotMarker, project);
				}
			}
			
			// create constant markers
			// TODO?
			//markConstants(sh.getAbstractValue(), hotspotMarker, project);
		}
		
		return hotspotMarker;
	}
	
	private static void removeOldMarkers(HotspotDescriptor hotspot) {
		try {
			IFile file = PositionUtil.getFile(hotspot.getPosition());
			
			IMarker[] markers = file.findMarkers(AlvorGuiPlugin.HOTSPOT_MARKER_ID, false, IResource.DEPTH_ZERO);
			for (IMarker marker : markers) {
				if (marker.getAttribute(IMarker.CHAR_START, 0) == hotspot.getPosition().getStart()) {
					deleteChildMarkers(marker);
					marker.delete();
				}
			}
		} 
		catch (CoreException e) {
			LOG.exception(e); 
		}
	}
	
	public static void deleteAlvorMarkers(IResource res) {
		
		try {
			// TODO if res is not project, then clear also "sub-markers"
			if (res instanceof IFile) {
				// find all hotspot markers and clear all child markers that are not in this file
				IMarker[] markers = res.findMarkers(AlvorGuiPlugin.HOTSPOT_MARKER_ID, true, IResource.DEPTH_INFINITE);
				for (IMarker marker : markers) {
					deleteChildMarkers(marker);
				}
			}
			
			
			// clear markers directly in this resource
			res.deleteMarkers(AlvorGuiPlugin.ERROR_MARKER_ID, true, IResource.DEPTH_INFINITE);
			res.deleteMarkers(AlvorGuiPlugin.WARNING_MARKER_ID, true, IResource.DEPTH_INFINITE);
			res.deleteMarkers(AlvorGuiPlugin.HOTSPOT_MARKER_ID, true, IResource.DEPTH_INFINITE);
			res.deleteMarkers(AlvorGuiPlugin.UNSUPPORTED_MARKER_ID, true, IResource.DEPTH_INFINITE);
			res.deleteMarkers(AlvorGuiPlugin.STRING_MARKER_ID, true, IResource.DEPTH_INFINITE);
		} 
		catch (CoreException e) {
			LOG.exception(e);
		}
	}
	
	private static void deleteChildMarkers(IMarker marker) throws CoreException {
		Object att = marker.getAttribute(CHILDREN_ATT_NAME);
		if (att == null) {
			return;
		}
		String[] childrenPathIds = att.toString().split(";");
		for (String childrenPathId : childrenPathIds) {
			String[] parts = childrenPathId.split(":");
			if (parts.length == 2) {
				IFile file = WorkspaceUtil.getFile(parts[0]);
				long markerId = Long.valueOf(parts[1]);
				IMarker childMarker = file.findMarker(markerId);
				if (childMarker != null) {
					childMarker.delete();
				}
			}
		}
	}
	
	private void createCheckingMarker(HotspotProblem checkingResult,	IMarker parentMarker, IProject project) {
		String markerId = null; 
		Integer severity = null;

		if (checkingResult.getProblemType() == HotspotProblem.ProblemType.ERROR) {
			markerId = AlvorGuiPlugin.ERROR_MARKER_ID;
			severity = IMarker.SEVERITY_ERROR;  
		}
		else {
			markerId = AlvorGuiPlugin.UNSUPPORTED_MARKER_ID;
			severity = IMarker.SEVERITY_INFO;  
		}
		createMarker(checkingResult.getMessage(), markerId, severity, 
				checkingResult.getPosition(), parentMarker, project); 
	}

//	/*
//	 * This method makes things slow on big projects, although on small ones the markers look nice
//	 */
//	private void markConstants(IAbstractString abstractValue, final IMarker parentMarker, final IProject project) {
//		IAbstractStringVisitor<Void, Void> visitor = new IAbstractStringVisitor<Void, Void>() {
//
//			@Override
//			public Void visitStringCharacterSet(
//					StringCharacterSet characterSet, Void data) {
//				createMarker("", AlvorGuiPlugin.STRING_MARKER_ID, null, 
//						characterSet.getPosition(), parentMarker, project);
//				return null;
//			}
//
//			@Override
//			public Void visitStringChoice(StringChoice stringChoice, Void data) {
//				for (IAbstractString s : stringChoice.getItems()) {
//					s.accept(this, null);
//				}
//				return null;
//			}
//
//			@Override
//			public Void visitStringConstant(StringConstant stringConstant,
//					Void data) {
//				createMarker("", AlvorGuiPlugin.STRING_MARKER_ID, null, 
//						stringConstant.getPosition(), parentMarker, project);
//				return null;
//			}
//
//			@Override
//			public Void visitStringParameter(StringParameter stringParameter,
//					Void data) {
//				return null;
//			}
//
//			@Override
//			public Void visitStringRepetition(
//					StringRepetition stringRepetition, Void data) {
//				stringRepetition.getBody().accept(this, null);
//				return null;
//			}
//
//			@Override
//			public Void visitStringSequence(StringSequence stringSequence,
//					Void data) {
//				for (IAbstractString s : stringSequence.getItems()) {
//					s.accept(this, null);
//				}
//				return null;
//			}
//
//			@Override
//			public Void visitStringRecursion(StringRecursion stringRecursion,
//					Void data) {
//				return null;
//			}
//		};
//		abstractValue.accept(visitor, null);
//	}

	private static IMarker createMarker(String message, String markerType, Integer severity, IPosition pos,
			IMarker parentMarker, IProject project) {
		
		IPosition adaptedPos = pos;
		if (adaptedPos == null) {
			adaptedPos = new Position(project.getFullPath().toPortableString(), 0, 0);
		}
		
		if (DummyPosition.isDummyPosition(adaptedPos)) {
			// FIXME this should not be required anymore
			LOG.error("Warning: Dummy position in 'createMarker'");
			return null;
		}
		
		// collect attributes of markers into this map
		Map<String, Comparable<?>> map = new HashMap<String, Comparable<?>>();
		if (severity != null) {
			map.put(IMarker.SEVERITY, severity);
		}
		
		// Seems that markers with too long messages are not shown
		String finalMessage = message;
		if (message.length() > 800) {
			finalMessage = message.substring(0, 800) + "...";
		}
		MarkerUtilities.setMessage(map, finalMessage);
		
		
		IResource res = PositionUtil.getPositionResource(adaptedPos);
		map.put(IMarker.LOCATION, res.getFullPath().toString());
		
		// include position info only when proper position is given
		int charStart = adaptedPos.getStart();
		int charEnd = charStart + adaptedPos.getLength();
		if (charStart > 0 || charEnd > 0) { 
			MarkerUtilities.setCharStart(map, charStart);
			MarkerUtilities.setCharEnd(map, charEnd);
		}
		

		
		try {
			IMarker marker = res.createMarker(markerType);
			// TODO do I need to store reference to parent ?
			marker.setAttributes(map);
			
			// register itself in parent marker
			if (parentMarker != null) {
				String markerPathId = res.getFullPath().toPortableString() + ":" + marker.getId();
				Object attr = parentMarker.getAttribute(CHILDREN_ATT_NAME);
				String childrenStr = "";
				if (attr != null) {
					childrenStr = attr.toString() + ";" + markerPathId;
				}
				else {
					childrenStr = markerPathId;
				}
				// TODO try setting attributes like "MarkerUtilities.createMarker" does, to see if it's faster
				parentMarker.setAttribute(CHILDREN_ATT_NAME, childrenStr);
			}
			
			return marker;
			//MarkerUtilities.createMarker(res, map, markerType);
		} catch (CoreException e) {
			LOG.exception(e);
			throw new RuntimeException(e);
		}
	}

	public void clearProject(IProject project) {
		deleteAlvorMarkers(project);
		CacheProvider.tryDeleteCache(project.getName());
	}
	
}
