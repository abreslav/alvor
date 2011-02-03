package com.zeroturnaround.alvor.crawler;

import org.junit.Test;

import com.zeroturnaround.alvor.cache.CacheService;

public class StringEvaluatorTest_Full_EArved extends AbstractStringEvaluatorTest {

	@Test
	@Override
	public void findValidNodeDescriptors() {
		CacheService.getCacheService().clearAll();
		NodeSearchEngine.clearASTCache();
		
		// TODO find why clean result is different from cached result
		
		findAndValidateNodeDescriptors("earved");
	}
}
