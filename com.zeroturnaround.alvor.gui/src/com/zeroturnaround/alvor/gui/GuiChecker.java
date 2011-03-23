package com.zeroturnaround.alvor.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.checkers.HotspotCheckingResult;
import com.zeroturnaround.alvor.checkers.HotspotError;
import com.zeroturnaround.alvor.checkers.HotspotInfo;
import com.zeroturnaround.alvor.checkers.HotspotWarning;
import com.zeroturnaround.alvor.checkers.complex.ComplexChecker;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.WorkspaceUtil;
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
	private ComplexChecker complexChecker = new ComplexChecker();
	private Cache cache = CacheProvider.getCache();
	private ProjectConfiguration conf;
	private int hotspotCount = 0;
	
	
	public void cleanUpdateProjectMarkers(IProject project, IProgressMonitor monitor) {
		cache.clearAll(); // FIXME clear project
		StringCollector.updateProjectCache(project, cache, null);
		List<HotspotDescriptor> hotspots = cache.getProjectHotspots(project.getName());
		clearAlvorMarkers(project);

		conf = ConfigurationManager.readProjectConfiguration(project, true);
		
		Collection<HotspotCheckingResult> checkingResults = 
			complexChecker.checkNodeDescriptors(hotspots, conf, monitor);
		
		createCheckingMarkers(checkingResults, project);
	}
		
	public void updateProjectMarkersForChangedFiles(IProject project, IProgressMonitor monitor) {
		StringCollector.updateProjectCache(project, cache, monitor);
		
		// TODO
		conf = ConfigurationManager.readProjectConfiguration(project, true);
		
		
		this.hotspotCount = 0;
		for (String fileName : cache.getUncheckedFiles(project.getName())) {
			updateFileMarkers(fileName, project, monitor);
		}
		
		System.out.println("Finished: Checked " + hotspotCount + " hotspots");
	}
	
	private void updateFileMarkers(String fileName, IProject project, IProgressMonitor monitor) {
		IFile file = WorkspaceUtil.getFile(fileName);
		Collection<HotspotDescriptor> hotspots = 
			cache.getFileHotspots(fileName, project.getName());
		
		this.hotspotCount += hotspots.size();
		
		// TODO delete and re-check only changed hotspots
		clearAlvorMarkers(file);
		
		if (conf.getMarkHotspots()) {
			markHotspots(hotspots);
		}
		
		Collection<HotspotCheckingResult> checkingResults = 
			complexChecker.checkNodeDescriptors(hotspots, conf, monitor);
		
		createCheckingMarkers(checkingResults, project);
	}
	
	private void clearAlvorMarkers(IResource res) {
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
				markerId = AlvorGuiPlugin.WARNING_MARKER_ID;
				severity = IMarker.SEVERITY_INFO;  
			}
			createMarker(result.getMessage(), markerId, severity, 
					preparePosition(result.getPosition(), currentProject));				
		}
	}

	private void markHotspots(Collection<HotspotDescriptor> hotspots) {
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
	
}
