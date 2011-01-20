package com.zeroturnaround.alvor.gui;

import org.junit.BeforeClass;

import com.zeroturnaround.alvor.crawler.CrawlerTestUtil;

public class FullMarkerTestEArved extends AbstractMarkerTest {
	
	@BeforeClass
	public static void prepare() throws Exception {
		// TODO should do it via GUI
		//GuiFacade.selectAnItemInPackageExplorer();
		//GuiFacade.executeAlvorCleanCheck();
		AbstractMarkerTest.selectedProject = CrawlerTestUtil.getProject("earved"); 
		GuiFacade.executeAlvorCleanCheck(AbstractMarkerTest.selectedProject);
		
		//GuiFacade.waitUntilAlvorHasCompleted();
	}
}
