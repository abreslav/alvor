package com.googlecode.alvor.gui;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


public class AlvorGuiPlugin extends AbstractUIPlugin {
	public static final String ID = "com.googlecode.alvor.gui";
	public static final String ERROR_MARKER_ID = "com.googlecode.alvor.gui.sqlerror";
	public static final String WARNING_MARKER_ID = "com.googlecode.alvor.gui.sqlwarning";
	public static final String HOTSPOT_MARKER_ID = "com.googlecode.alvor.gui.sqlhotspot";
	public static final String UNSUPPORTED_MARKER_ID = "com.googlecode.alvor.gui.unsupported";
	public static final String STRING_MARKER_ID = "com.googlecode.alvor.gui.sqlstring";
	
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
		defaultInstance = null;
		super.stop(context);
	}
	
	public static AlvorGuiPlugin getDefault() {
		return defaultInstance;
	}
	
}
