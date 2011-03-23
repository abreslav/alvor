package com.zeroturnaround.alvor.gui;

import org.eclipse.core.resources.IProject;
import org.junit.BeforeClass;

import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.WorkspaceUtil;

public class EarvedGuiChecker2TestNew extends AbstractMarkerTest {

	@BeforeClass
	public static void createMarkers() {
		IProject project = WorkspaceUtil.getProject("earved");
		CacheProvider.getCache().clearAll();
		
		GuiChecker2 checker = new GuiChecker2();
//		checker.updateProjectMarkersForChangedFiles(project, null);
		checker.cleanUpdateProjectMarkers(project, null);
		
		AbstractMarkerTest.selectedProject = project;
	}
}
