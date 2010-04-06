package ee.stacc.productivity.edsl.javaproject;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.gui.CheckProjectHandler;

public class Activator extends AbstractUIPlugin {

	@Override
	public void start(BundleContext context) throws Exception {
		Logs.configureFromStream(CheckProjectHandler.class.getClassLoader().getResourceAsStream("logging.properties"));
		super.start(context);
	}
}
