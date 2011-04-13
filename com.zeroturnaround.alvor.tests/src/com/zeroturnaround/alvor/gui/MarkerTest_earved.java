package com.zeroturnaround.alvor.gui;

import org.junit.Test;

import com.zeroturnaround.alvor.tests.util.ProjectBasedTester;

public class MarkerTest_earved {
	@Test
	public void testMarkers() {
		ProjectBasedTester.runOn("earved", false, true);
	}
}
