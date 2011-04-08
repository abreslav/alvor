package com.zeroturnaround.alvor.gui;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.Test;

import com.zeroturnaround.alvor.tests.util.CrawlerTestUtil;
import com.zeroturnaround.alvor.tests.util.GuiFacade;

/**
 * Base class for tests which check the validity of markers produced by Alvor
 * @author Aivar
 *
 */
public abstract class AbstractMarkerTest {
	
	// Concrete classes should add a static method with @BeforeClass annotation
	// where it selects the project (assigns AbstractMarkersTest.project) 
	// and performs the checking
	
	protected static IProject selectedProject; 
	
	@Test
	public void verifyErrorMarkers() throws CoreException {
		storeAndVerifyMarkers(AlvorGuiPlugin.ERROR_MARKER_ID);
	}
	
	@Test
	public void verifyWarningMarkers() throws CoreException {
		storeAndVerifyMarkers(AlvorGuiPlugin.WARNING_MARKER_ID);
		// TODO: check also correspondence to crawler test output 
	}
	
	@Test
	public void verifyHotspotMarkers() throws CoreException {
		storeAndVerifyMarkers(AlvorGuiPlugin.HOTSPOT_MARKER_ID);
		// TODO: check also correspondence to crawler test output 
	}
	
	private void storeAndVerifyMarkers(String markerId) throws CoreException {
		List<String> markers = GuiFacade.getMarkersAsStrings(AbstractMarkerTest.selectedProject, markerId);
		Collections.sort(markers);
		
		File folder = CrawlerTestUtil.getAndPrepareTestResultsFolder(AbstractMarkerTest.selectedProject);
		String shortId = markerId.substring(markerId.lastIndexOf('.')+1);
		String filePrefix = folder.getAbsolutePath() + "/" + shortId;
		
		Assert.assertTrue("Generated markers differ from expected markers",
				CrawlerTestUtil.stringsAreExpected(markers, filePrefix));
	}
	
}
