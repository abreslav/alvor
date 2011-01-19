package com.zeroturnaround.alvor.gui;

import org.junit.BeforeClass;

import com.zeroturnaround.alvor.crawler.testutils.TestUtil;

public class FullMarkerTestSqlBus extends AbstractMarkerTest {
	
	@BeforeClass
	public static void prepare() throws Exception {
		AbstractMarkerTest.selectedProject = TestUtil.getProject("sql_bus"); 
		GuiFacade.executeAlvorCleanCheck(AbstractMarkerTest.selectedProject);
	}
}
