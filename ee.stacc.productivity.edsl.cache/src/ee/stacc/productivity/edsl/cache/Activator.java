package ee.stacc.productivity.edsl.cache;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	public Activator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		CacheService.getCacheService().shutdown();
		super.stop(context);
	}
	
}
