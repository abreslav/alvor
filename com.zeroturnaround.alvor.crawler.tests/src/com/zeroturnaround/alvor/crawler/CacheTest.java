package com.zeroturnaround.alvor.crawler;

import org.junit.Test;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.CacheProvider;

public class CacheTest {

	@Test
	public void testCreation() {
		Cache cache = CacheProvider.getCache();
	}
}
