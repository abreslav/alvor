package com.zeroturnaround.alvor.gui.slowtests;

import org.junit.BeforeClass;

import com.zeroturnaround.alvor.common.WorkspaceUtil;
import com.zeroturnaround.alvor.gui.AbstractMarkerTest;
import com.zeroturnaround.alvor.tests.util.GuiFacade;

public class FullMarkerTestSqlBus extends AbstractMarkerTest {
	
	@BeforeClass
	public static void prepare() throws Exception {
		AbstractMarkerTest.selectedProject = WorkspaceUtil.getProject("sql_bus"); 
		GuiFacade.executeAlvorCleanCheck(AbstractMarkerTest.selectedProject);
	}
}
