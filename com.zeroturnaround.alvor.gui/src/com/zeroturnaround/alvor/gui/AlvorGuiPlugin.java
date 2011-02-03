package com.zeroturnaround.alvor.gui;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.zeroturnaround.alvor.cache.CacheService;


public class AlvorGuiPlugin extends AbstractUIPlugin {
	public static final String ID = "com.zeroturnaround.alvor.gui";
	public static final String ERROR_MARKER_ID = "com.zeroturnaround.alvor.gui.sqlerror";
	public static final String WARNING_MARKER_ID = "com.zeroturnaround.alvor.gui.sqlwarning";
	public static final String HOTSPOT_MARKER_ID = "com.zeroturnaround.alvor.gui.sqlhotspot";
	public static final String UNSUPPORTED_MARKER_ID = "com.zeroturnaround.alvor.gui.unsupported";
	public static final String STRING_MARKER_ID = "com.zeroturnaround.alvor.gui.sqlstring";
	
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
