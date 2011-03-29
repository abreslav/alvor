package com.zeroturnaround.alvor.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.checkers.CheckerException;
import com.zeroturnaround.alvor.checkers.HotspotCheckingResult;
import com.zeroturnaround.alvor.checkers.HotspotError;
import com.zeroturnaround.alvor.checkers.HotspotInfo;
import com.zeroturnaround.alvor.checkers.HotspotWarning;
import com.zeroturnaround.alvor.checkers.complex.ComplexChecker;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.configuration.ConfigurationManager;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.crawler.StringCollector;
import com.zeroturnaround.alvor.string.DummyPosition;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.Position;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRecursion;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;
import com.zeroturnaround.alvor.string.util.AbstractStringOptimizer;

public class GuiChecker {
	private static final ILog LOG = Logs.getLog(GuiChecker.class);
	private ComplexChecker checker = new ComplexChecker();
	private Cache cache = CacheProvider.getCache();
	
	
	public void cleanUpdateProjectMarkers(IProject project, IProgressMonitor monitor) {
		cache.clearProject(project.getName());
		clearAlvorMarkers(project);
		updateProjectMarkers(project, monitor);
	}
	
	public void updateProjectMarkers(IProject project, IProgressMonitor monitor) {
		// assumes that markers of invalidated files are already deleted
		
		StringCollector.updateProjectCache(project, cache, monitor);
		
		ProjectConfiguration conf = ConfigurationManager.readProjectConfiguration(project, true);
		Collection<HotspotDescriptor> hotspots = 
			cache.getUncheckedPrimaryProjectHotspots(project.getName());
		
		
		try {
			for (HotspotDescriptor hotspot : hotspots) {
				Collection<HotspotCheckingResult> checkingResults = checker.checkHotspot(hotspot, conf);
				createCheckingMarkers(checkingResults, project);
			}
			cache.markHotspotsAsChecked(hotspots);
		} 
		catch (CheckerException e) {
			createMarker("Checker exception: " + e.getMessage(), AlvorGuiPlugin.ERROR_MARKER_ID,
					IMarker.SEVERITY_ERROR, e.getPosition(), project);
		}
		
		// TODO clean orphaned (constant) markers
	}
	
	public static void clearAlvorMarkers(IResource res) {
		// TODO if res is not project, then clear also "sub-markers"
		
		try {
			res.deleteMarkers(AlvorGuiPlugin.ERROR_MARKER_ID, true, IResource.DEPTH_INFINITE);
			res.deleteMarkers(AlvorGuiPlugin.WARNING_MARKER_ID, true, IResource.DEPTH_INFINITE);
			res.deleteMarkers(AlvorGuiPlugin.HOTSPOT_MARKER_ID, true, IResource.DEPTH_INFINITE);
			res.deleteMarkers(AlvorGuiPlugin.UNSUPPORTED_MARKER_ID, true, IResource.DEPTH_INFINITE);
			res.deleteMarkers(AlvorGuiPlugin.STRING_MARKER_ID, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	private void createCheckingMarkers(Collection<HotspotCheckingResult> checkingResults,
			IProject project) {
		
		for (HotspotCheckingResult result : checkingResults) {
			String markerId = null; 
			Integer severity = null;
			
			if (result instanceof HotspotError) {
				markerId = AlvorGuiPlugin.ERROR_MARKER_ID;
				severity = IMarker.SEVERITY_ERROR;  
			}
			else if (result instanceof HotspotWarning) {
				markerId = AlvorGuiPlugin.WARNING_MARKER_ID;
				severity = IMarker.SEVERITY_WARNING;  
			}
			else if (result instanceof HotspotInfo) {
				markerId = AlvorGuiPlugin.WARNING_MARKER_ID;
				severity = IMarker.SEVERITY_INFO;  
			}
			createMarker(result.getMessage(), markerId, severity, result.getPosition(), project); 
		}
	}

	@Deprecated
	private void markHotspots(Collection<HotspotDescriptor> hotspots, IProject project) {
		for (HotspotDescriptor hotspot : hotspots) {
			String message;
			String markerId;
			if (hotspot instanceof StringNodeDescriptor) {
				StringNodeDescriptor snd = (StringNodeDescriptor) hotspot;
				IAbstractString abstractValue = snd.getAbstractValue();
				message = "Abstract string: " + CommonNotationRenderer.render(AbstractStringOptimizer.optimize(abstractValue));
				if (message.length() > Character.MAX_VALUE) {
					message = message.substring(0, Character.MAX_VALUE - 3) + "...";
				}
				markerId = AlvorGuiPlugin.HOTSPOT_MARKER_ID;
				
				// TODO
				//markConstants(abstractValue);
			} else if (hotspot instanceof UnsupportedNodeDescriptor) {
				UnsupportedNodeDescriptor und = (UnsupportedNodeDescriptor) hotspot;
				message = "Unsupported construction: " + und.getProblemMessage();
				markerId = AlvorGuiPlugin.UNSUPPORTED_MARKER_ID;
			} else {
				throw new IllegalArgumentException(hotspot + "");
			}
			createMarker(
					message, 
					markerId,
					IMarker.SEVERITY_INFO,
					hotspot.getPosition(), project);
		}		
	}

	/*
	 * This method makes things slow on big projects, although on small ones the markers look nice
	 */
	@Deprecated
	private void markConstants(IAbstractString abstractValue, final IProject project) {
		IAbstractStringVisitor<Void, Void> visitor = new IAbstractStringVisitor<Void, Void>() {

			@Override
			public Void visitStringCharacterSet(
					StringCharacterSet characterSet, Void data) {
				createMarker("", AlvorGuiPlugin.STRING_MARKER_ID, null, characterSet.getPosition(), project);
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
				createMarker("", AlvorGuiPlugin.STRING_MARKER_ID, null, stringConstant.getPosition(), null);
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

			@Override
			public Void visitStringRecursion(StringRecursion stringRecursion,
					Void data) {
				return null;
			}
		};
		abstractValue.accept(visitor, null);
	}

	private static void createMarker(String message, String markerType, Integer severity, IPosition pos2,
			IProject project) {
		
		IPosition pos = pos2;
		if (pos == null) {
			pos = new Position(project.getFullPath().toPortableString(), 0, 0);
		}
		
		if (DummyPosition.isDummyPosition(pos)) {
			// FIXME this should not be required anymore
			LOG.error("Warning: Dummy position in 'createMarker'");
			return;
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
		
		
		IResource res = PositionUtil.getPositionResource(pos);
		map.put(IMarker.LOCATION, res.getFullPath().toString());
		
		
		// include position info only when there is proper position given
		int charStart = pos.getStart();
		int charEnd = charStart + pos.getLength();
		if (charStart > 0 || charEnd > 0) { 
			MarkerUtilities.setCharStart(map, charStart);
			MarkerUtilities.setCharEnd(map, charEnd);
		}

		
		try {
			MarkerUtilities.createMarker(res, map, markerType);
		} catch (Exception e) {
			LOG.exception(e);
		}
	}
	
}
