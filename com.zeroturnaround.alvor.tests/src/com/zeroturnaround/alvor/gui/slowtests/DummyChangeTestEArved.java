package com.zeroturnaround.alvor.gui.slowtests;

import org.junit.BeforeClass;

import com.zeroturnaround.alvor.common.WorkspaceUtil;
import com.zeroturnaround.alvor.gui.AbstractMarkerTest;
import com.zeroturnaround.alvor.tests.util.GuiFacade;

public class DummyChangeTestEArved extends AbstractMarkerTest {

	@BeforeClass
	public static void prepare() throws Exception {
		AbstractMarkerTest.selectedProject = WorkspaceUtil.getProject("earved"); 
		GuiFacade.executeAlvorCleanCheck(AbstractMarkerTest.selectedProject);
		
		// This file has errors
		GuiFacade.touchFile("/earved/src/ee/post/earved/dao/invoice/AttachmentDAO.java");
		
		// This file has hotspot but doesn't have errors
		GuiFacade.touchFile("/earved/src/ee/post/earved/dao/invoice/InvoiceSendingFailedDAO.java");
//		
//		// This file doesn't have hotspots
		GuiFacade.touchFile("/earved/src/ee/post/earved/dao/invoice/InvoiceSendingFailedDAO.java");
//		
//		// don't proceed until the checking has finished
		GuiFacade.waitUntilAlvorHasCompleted();
		
		
		// NB! check from console, that builder was really invoked by these touches
	}
	
}
