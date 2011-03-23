package com.zeroturnaround.alvor.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.zeroturnaround.alvor.cache.CacheService;
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
import com.zeroturnaround.alvor.crawler.AbstractStringEvaluator;
import com.zeroturnaround.alvor.crawler.NodeSearchEngine;
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
	private ComplexChecker complexChecker = new ComplexChecker();
	
	
	public void performCleanCheck(IProject optionsFrom, IJavaElement[] scope,
			IProgressMonitor monitor) {
		NodeSearchEngine.clearASTCache();
		CacheService.getCacheService().clearAll();
		performIncrementalCheck(optionsFrom, scope, monitor);
	}
	
	/**
	 * NB! Before calling this you should take care that NodeSearchEngine's ASTCache doesn't
	 * contain old stuff (either clear it completely or remove expired AST-s)
	 */
	public void performIncrementalCheck(IProject currentProject, IJavaElement[] scope, 
			IProgressMonitor monitor) {
		if (scope.length == 0) {
			return;
		}
		
		if (monitor != null) {
			monitor.beginTask("Checking SQL", 2000);
		}
		
		try {
			cleanMarkers(scope);
			cleanConfigurationMarkers(currentProject);
			
			ProjectConfiguration conf = ConfigurationManager.readProjectConfiguration(currentProject, true);
			List<HotspotDescriptor> hotspots = AbstractStringEvaluator.findAndEvaluateHotspots(scope, conf, monitor);
			
			if (conf.getMarkHotspots()) {
				markHotspots(hotspots);
			}
			
			Collection<HotspotCheckingResult> checkingResults = 
				complexChecker.checkNodeDescriptors(hotspots, conf, monitor);
			
			createCheckingMarkers(checkingResults, currentProject);
		}
		finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	
	private void cleanConfigurationMarkers(IProject project) {
		try {
			project.deleteMarkers(AlvorGuiPlugin.ERROR_MARKER_ID, true, IResource.DEPTH_ONE);
			project.deleteMarkers(AlvorGuiPlugin.WARNING_MARKER_ID, true, IResource.DEPTH_ONE);
		} catch (CoreException e) {
			LOG.error("Cleaning markers", e);
		}
	}

	private void cleanMarkers(IJavaElement[] scope) {
		try {
			for (IJavaElement element : scope) {
				element.getResource().deleteMarkers(AlvorGuiPlugin.ERROR_MARKER_ID, true, IResource.DEPTH_INFINITE);
				element.getResource().deleteMarkers(AlvorGuiPlugin.WARNING_MARKER_ID, true, IResource.DEPTH_INFINITE);
				element.getResource().deleteMarkers(AlvorGuiPlugin.HOTSPOT_MARKER_ID, true, IResource.DEPTH_INFINITE);
				element.getResource().deleteMarkers(AlvorGuiPlugin.UNSUPPORTED_MARKER_ID, true, IResource.DEPTH_INFINITE);
				element.getResource().deleteMarkers(AlvorGuiPlugin.STRING_MARKER_ID, true, IResource.DEPTH_INFINITE);
			}
		} catch (Exception e) {
			LOG.exception(e);
		}
	}
	
	private static void createMarker(String message, String markerType, Integer severity, IPosition pos) {
		
		if (DummyPosition.isDummyPosition(pos)) {
			// TODO get rid of this situation
			LOG.message("Warning: Dummy position in 'createMarker'");
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
	
	private void createCheckingMarkers(Collection<HotspotCheckingResult> checkingResults,
			IProject currentProject) {
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
				markerId = AlvorGuiPlugin.HOTSPOT_MARKER_ID;
				severity = IMarker.SEVERITY_INFO;  
			}
			createMarker(result.getMessage(), markerId, severity, 
					preparePosition(result.getPosition(), currentProject));				
		}
	}

	private void markHotspots(List<HotspotDescriptor> hotspots) {
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
				markConstants(abstractValue);
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
					hotspot.getPosition());
		}		
	}

	/*
	 * This method makes things slow on big projects, although on small ones the markers look nice
	 */
	private void markConstants(IAbstractString abstractValue) {
		IAbstractStringVisitor<Void, Void> visitor = new IAbstractStringVisitor<Void, Void>() {

			@Override
			public Void visitStringCharacterSet(
					StringCharacterSet characterSet, Void data) {
				createMarker("", AlvorGuiPlugin.STRING_MARKER_ID, null, preparePosition(characterSet.getPosition(), null));
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
				createMarker("", AlvorGuiPlugin.STRING_MARKER_ID, null, preparePosition(stringConstant.getPosition(), null));
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

	private IPosition preparePosition(IPosition pos, IProject currentProject) {
		if (pos == null) {
			if (currentProject == null) {
				throw new IllegalArgumentException("Can't create marker when both position and project are null");
			}
			return new Position(currentProject.getFullPath().toPortableString(), 0, 0);
		}
		else {
			return pos;
		}
	}
}
