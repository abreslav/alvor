package ee.stacc.productivity.edsl.gui;


import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ee.stacc.productivity.edsl.common.logging.Logs;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin  {

	// The plug-in ID
	public static final String PLUGIN_ID = "ASTPlug2";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}
	

	@Override
	public void start( final BundleContext context ) throws Exception {
		super.start( context );
		Logs.configureFromStream(CheckProjectHandler.class.getClassLoader().getResourceAsStream("logging.properties"));
	    plugin = this;
	}
	
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}
}
