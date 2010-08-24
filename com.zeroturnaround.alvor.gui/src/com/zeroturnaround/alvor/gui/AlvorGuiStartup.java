package ee.stacc.productivity.edsl.gui;

import org.eclipse.ui.IStartup;

import ee.stacc.productivity.edsl.common.logging.Logs;

public class EdslGuiStartup implements IStartup {

	@Override
	public void earlyStartup() {
		Logs.configureFromStream(CheckProjectHandler.class.getClassLoader().getResourceAsStream("logging.properties"));
	}

}
