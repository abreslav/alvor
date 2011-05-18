package com.googlecode.alvor.common;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class AlvorCommonPlugin extends Plugin {
	public static final String ID = "com.googlecode.alvor.common";
	
	private static AlvorCommonPlugin defaultInstance;
	
	public AlvorCommonPlugin() {
	}

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
	
	public static AlvorCommonPlugin getDefault() {
		return defaultInstance;
	}

}
