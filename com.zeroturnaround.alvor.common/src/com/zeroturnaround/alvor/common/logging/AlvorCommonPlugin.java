package ee.stacc.productivity.edsl.common.logging;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


public class EDSLCommonPlugin extends Plugin {

	public static final String ID = "ee.stacc.productivity.edsl.common";
	
	private static EDSLCommonPlugin defaultInstance;
	
	public EDSLCommonPlugin() {
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
	
	public static EDSLCommonPlugin getDefault() {
		return defaultInstance;
	}

}
