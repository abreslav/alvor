package ee.stacc.productivity.edsl.cache;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class EDSLCachePlugin extends Plugin {

	public static final String ID = "ee.stacc.productivity.edsl.cache";
	
	private static EDSLCachePlugin defaultInstance;
	
	public EDSLCachePlugin() {
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
	
	public static EDSLCachePlugin getDefault() {
		return defaultInstance;
	}
	
}
