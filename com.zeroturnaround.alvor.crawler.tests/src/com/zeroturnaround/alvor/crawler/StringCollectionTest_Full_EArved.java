package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.cache.CacheProvider;

public class StringCollectionTest_Full_EArved extends
		AbstractStringCollectionTest {

	@Override
	public void findValidNodeDescriptors() {
		String projectName = "earved";
		CacheProvider.getCache().clearProject(projectName);
		findAndValidateNodeDescriptors(projectName);
	}

}
