package com.zeroturnaround.alvor.gui;

import org.junit.BeforeClass;

import com.zeroturnaround.alvor.crawler.CrawlerTestUtil;

public class FullMarkerTestSqlBus extends AbstractMarkerTest {
	
	@BeforeClass
	public static void prepare() throws Exception {
		AbstractMarkerTest.selectedProject = CrawlerTestUtil.getProject("sql_bus"); 
		GuiFacade.executeAlvorCleanCheck(AbstractMarkerTest.selectedProject);
	}
}
