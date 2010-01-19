package ee.stacc.productivity.edsl.gui;


import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

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
	        plugin = this;
	        
	        /* ei tööta:
	        ICommandService commandService = (ICommandService)plugin.getWorkbench().getService( ICommandService.class );
	        commandService.addExecutionListener( new IExecutionListener() {

	                public void notHandled( final String commandId, final NotHandledException exception ) {}

	                public void postExecuteFailure( final String commandId, final ExecutionException exception ) {}

	                public void postExecuteSuccess( final String commandId, final Object returnValue ) {
	                        if ( commandId.equals( "org.eclipse.ui.file.save" ) ) {
	                        	System.err.println("jura");
	                                // add in your action here...
	                                // personally, I would use a custom preference page, 
	                                // but hard coding would work ok too
	                        }
	                }

	                public void preExecute( final String commandId, final ExecutionEvent event ) {}

	        } );
	        */
	}
	
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}
}
