package ee.stacc.productivity.edsl.common.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Platform;

public class Logs {

	private static boolean configured = false;
	
	private static final Map<String, ILog> OPTIONS = new HashMap<String, ILog>();

	public static void configureFromStream(InputStream propStream) {
		if (propStream == null) {
			defaultConfiguration();
		} else {
			if (configured) {
				return;
			}
			configured = true;
			try {
				Properties properties = new Properties();
				properties.load(propStream);
				
				String allToOutStr = properties.getProperty("$log.all.to.out", "false");
				boolean allToOut = "true".equals(allToOutStr);
				
				String logRootDirStr = properties.getProperty("$log.root.dir", ".");
				if (Platform.isRunning()) {
					logRootDirStr = logRootDirStr.replace("$workspace", EDSLCommonPlugin.getDefault().getStateLocation().toOSString());
//					logRootDirStr = logRootDirStr.replace("$workspace", ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString());
				}
				File logRootDir = new File(logRootDirStr);
				
				Map<String, ILog> createdLogs = new HashMap<String, ILog>();
				for (Entry<Object, Object> entry : properties.entrySet()) {
					String key = String.valueOf(entry.getKey());
					String value = String.valueOf(entry.getValue()).trim();
					if (!key.startsWith("$")) {
						ILog log;
						if ("$System.out".equals(value)) {
							log = PrintStreamLog.SYSTEM_OUT;
						} else if ("$System.err".equals(value)) {
							log = PrintStreamLog.SYSTEM_ERR;
						} else {
							log = createdLogs.get(value);
							if (log == null) {
								log = new PrintStreamLog(new PrintStream(new File(logRootDir, value)));
								createdLogs.put(value, log);
							}
						}
						if (allToOut) {
							if (log != PrintStreamLog.SYSTEM_OUT) {
								log = new CompositeLog(log, PrintStreamLog.SYSTEM_OUT);
							}
						}
						OPTIONS.put(key, log);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				defaultConfiguration();
			} catch (IOException e) {
				e.printStackTrace();
				defaultConfiguration();
			} finally { 
				try {
					propStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void defaultConfiguration() {
		if (configured) {
			return;
		}
		configured = true;
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.err.println("Logging falls back to default configuration");
		try {
			throw new IllegalStateException();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		OPTIONS.clear();
		OPTIONS.put("", PrintStreamLog.SYSTEM_ERR);
	}
	
	public static ILog getLog(Class<?> clazz) {
		return new AlternativeLog(clazz);
//		defaultConfiguration();
//		ILog log = OPTIONS.get(clazz.getCanonicalName());
//		if (log != null) {
//			return log;
//		}
//
//		String pack = clazz.getPackage().getName();
//		while (true) {
//			log = OPTIONS.get(pack);
//			if (log != null) {
//				return log;
//			}
//			int lastIndexOf = pack.lastIndexOf('.');
//			if (lastIndexOf < 0) {
//				break;
//			}
//			pack = pack.substring(0, lastIndexOf);
//		}
//		return OPTIONS.get("");
	}
}