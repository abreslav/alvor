package com.zeroturnaround.alvor.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.zeroturnaround.alvor.cache.CacheService;
import com.zeroturnaround.alvor.checkers.AbstractStringCheckingResult;
import com.zeroturnaround.alvor.checkers.AbstractStringWarning;
import com.zeroturnaround.alvor.checkers.complex.ComplexChecker;
import com.zeroturnaround.alvor.common.NodeDescriptor;
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
	
	
	public void performCleanCheck(IProject optionsFrom, IJavaElement[] scope) {
		NodeSearchEngine.clearASTCache();
		CacheService.getCacheService().clearAll();
		performIncrementalCheck(optionsFrom, scope);
	}
	
	/**
	 * NB! Before calling this you should take care that NodeSearchEngine's ASTCache doesn't
	 * contain old stuff (either clear it completely or remove expired AST-s)
	 */
	public void performIncrementalCheck(IProject currentProject, IJavaElement[] scope) {
		
		if (scope.length == 0) {
			return;
		}
		
		cleanMarkers(scope);
		cleanConfigurationMarkers(currentProject);
		
		ProjectConfiguration conf = ConfigurationManager.readProjectConfiguration(currentProject, true);
		List<NodeDescriptor> hotspots = AbstractStringEvaluator.findAndEvaluateHotspots(scope, conf);
		markHotspots(hotspots);
		
		Collection<AbstractStringCheckingResult> checkingResults = 
			complexChecker.checkNodeDescriptors(hotspots, conf);
		
		createErrorAndWarningMarkers(checkingResults, currentProject);
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
	
	private static void createMarker(String message, String markerType, IPosition pos) {
	
		Integer severity = null;
		
		if (markerType.equals(AlvorGuiPlugin.ERROR_MARKER_ID)) {
			severity = IMarker.SEVERITY_ERROR;
		}
		else if (markerType.equals(AlvorGuiPlugin.WARNING_MARKER_ID)) {
			severity = IMarker.SEVERITY_WARNING;
		} 
		
		createMarker(message, markerType, severity, pos);
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
	
	private void createErrorAndWarningMarkers(Collection<AbstractStringCheckingResult> checkingResults,
			IProject currentProject) {
		for (AbstractStringCheckingResult result : checkingResults) {
			String markerId = AlvorGuiPlugin.ERROR_MARKER_ID;
			if (result instanceof AbstractStringWarning) {
				markerId = AlvorGuiPlugin.WARNING_MARKER_ID;
			}
			createMarker(result.getMessage(), markerId, preparePosition(result.getPosition(), currentProject));				
		}
	}

	private void markHotspots(List<NodeDescriptor> hotspots) {
		for (NodeDescriptor hotspot : hotspots) {
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
				createMarker("", AlvorGuiPlugin.STRING_MARKER_ID, preparePosition(characterSet.getPosition(), null));
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
				createMarker("", AlvorGuiPlugin.STRING_MARKER_ID, preparePosition(stringConstant.getPosition(), null));
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
