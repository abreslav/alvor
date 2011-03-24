package com.zeroturnaround.alvor.crawler;

import com.zeroturnaround.alvor.cache.CacheProvider;

public class StringCollectionTest_Full_EArved extends
		AbstractStringCollectionTest {

	@Override
	public void findValidNodeDescriptors() {
		try {
			String projectName = "earved";
			// CacheProvider.getCache().clearProject(projectName);
			CacheProvider.getCache().clearAll();
			findAndValidateNodeDescriptors(projectName);
		}
		finally {
			CacheProvider.getCache().printDBInfo();
			CacheProvider.shutdownCache();
		}
	}

}
