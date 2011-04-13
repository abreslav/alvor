package com.zeroturnaround.alvor.gui;

import org.junit.Test;

import com.zeroturnaround.alvor.tests.util.ProjectBasedTester;

public class ChangeTest_Simple {
	@Test
	public void testMarkers() {
		ProjectBasedTester.runOn("SimpleChangeTest", ProjectBasedTester.TestScenario.INCREMENTAL, 
				ProjectBasedTester.TestSubject.MARKERS);
	}
}
