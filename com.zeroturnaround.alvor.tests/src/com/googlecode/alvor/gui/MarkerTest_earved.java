package com.googlecode.alvor.gui;

import org.junit.Test;

import com.googlecode.alvor.tests.util.ProjectBasedTester;

public class MarkerTest_earved {
	@Test
	public void testMarkers() {
		ProjectBasedTester.runOn("earved", ProjectBasedTester.TestScenario.CLEAN, 
				ProjectBasedTester.TestSubject.MARKERS);
	}
}
