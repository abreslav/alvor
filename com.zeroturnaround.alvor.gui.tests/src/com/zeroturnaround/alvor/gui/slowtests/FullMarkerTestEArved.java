package com.zeroturnaround.alvor.gui.slowtests;

import org.junit.BeforeClass;

import com.zeroturnaround.alvor.common.WorkspaceUtil;
import com.zeroturnaround.alvor.gui.AbstractMarkerTest;
import com.zeroturnaround.alvor.gui.GuiFacade;

public class FullMarkerTestEArved extends AbstractMarkerTest {
	
	@BeforeClass
	public static void prepare() throws Exception {
		// TODO should do it via GUI
		//GuiFacade.selectAnItemInPackageExplorer();
		//GuiFacade.executeAlvorCleanCheck();
		AbstractMarkerTest.selectedProject = WorkspaceUtil.getProject("earved"); 
		GuiFacade.executeAlvorCleanCheck(AbstractMarkerTest.selectedProject);
		
		//GuiFacade.waitUntilAlvorHasCompleted();
	}
}
