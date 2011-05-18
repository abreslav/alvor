package com.googlecode.alvor.gui;

import org.junit.Test;

import com.googlecode.alvor.tests.util.ProjectBasedTester;

public class ChangeTest_Simple {
	@Test
	public void testMarkers() {
		ProjectBasedTester.runOn("SimpleChangeTest", ProjectBasedTester.TestScenario.INCREMENTAL, 
				ProjectBasedTester.TestSubject.MARKERS);
	}
}
