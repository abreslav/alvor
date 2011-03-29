package com.zeroturnaround.alvor.gui;

import org.eclipse.core.resources.IProject;
import org.junit.BeforeClass;

import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.WorkspaceUtil;

public class EarvedGuiChecker2TestNew extends AbstractMarkerTest {

	@BeforeClass
	public static void createMarkers() {
		IProject project = WorkspaceUtil.getProject("earved");
		CacheProvider.getCache().clearAllProjects();
		
		try {
			GuiChecker checker = new GuiChecker();
//			checker.updateProjectMarkersForChangedFiles(project, null);
			checker.cleanUpdateProjectMarkers(project, null);
		}
//		catch (Exception e) {
//			
//		}
		finally {
			CacheProvider.getCache().printDBInfo();
			AbstractMarkerTest.selectedProject = project;
		}
	}
}
