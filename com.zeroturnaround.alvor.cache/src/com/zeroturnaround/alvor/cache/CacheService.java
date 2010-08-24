package com.zeroturnaround.alvor.cache;



public class CacheService {
	
	private final static ICacheService SERVICE  = new CacheServiceImpl(new HSQLDBLayer());
	
	public static ICacheService getCacheService() {
		return SERVICE;
	}
}
