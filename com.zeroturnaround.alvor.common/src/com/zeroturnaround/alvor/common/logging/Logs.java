package com.zeroturnaround.alvor.common.logging;

import java.util.HashMap;
import java.util.Map;


public class Logs {
	private static Map<String, ILog> logMap = new HashMap<String, ILog>();
	public static ILog getLog(Class<?> clazz) {
		String name = clazz.getCanonicalName(); 
		if (!logMap.containsKey(name)) {
			logMap.put(name, new LogImpl(name));
		}
		return logMap.get(name);
	}
}
