package com.zeroturnaround.alvor.cache;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class AlvorCachePlugin extends Plugin {

	public static final String ID = "com.zeroturnaround.alvor.cache";
	
	private static AlvorCachePlugin defaultInstance;
	
	public AlvorCachePlugin() {
	}

	@Override
	public void start( final BundleContext context ) throws Exception {
		super.start( context );
	    defaultInstance = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		CacheService.getCacheService().shutdown();
		defaultInstance = null;
		super.stop(context);
	}
	
	public static AlvorCachePlugin getDefault() {
		return defaultInstance;
	}
	
}
