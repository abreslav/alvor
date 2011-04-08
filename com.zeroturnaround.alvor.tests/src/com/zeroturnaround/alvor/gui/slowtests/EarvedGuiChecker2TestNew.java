package com.zeroturnaround.alvor.gui.slowtests;

import org.eclipse.core.resources.IProject;
import org.junit.BeforeClass;

import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.WorkspaceUtil;
import com.zeroturnaround.alvor.gui.AbstractMarkerTest;
import com.zeroturnaround.alvor.gui.GuiChecker;

public class EarvedGuiChecker2TestNew extends AbstractMarkerTest {

	@BeforeClass
	public static void createMarkers() {
		IProject project = WorkspaceUtil.getProject("earved");
		CacheProvider.getCache().clearAllProjects();
		
		try {
//			checker.updateProjectMarkersForChangedFiles(project, null);
			GuiChecker.INSTANCE.cleanUpdateProjectMarkers(project, null);
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
