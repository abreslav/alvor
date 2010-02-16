package ee.stacc.productivity.edsl.gui;

import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;

public class Logger {
	public static final ILog LOG;
	static {
		Logs.configureFromStream(CheckProjectHandler.class.getClassLoader().getResourceAsStream("logging.properties"));
		LOG = Logs.getLog(CheckProjectHandler.class);
	}
	

}
