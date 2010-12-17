package com.zeroturnaround.alvor.gui;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.zeroturnaround.alvor.cache.CacheService;


public class AlvorGuiPlugin extends AbstractUIPlugin {
	public static final String ID = "com.zeroturnaround.alvor.gui";
	
	public AlvorGuiPlugin() {
	}
	
	private static AlvorGuiPlugin defaultInstance;
	
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
	
	public static AlvorGuiPlugin getDefault() {
		return defaultInstance;
	}
	
}
