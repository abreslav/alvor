package com.zeroturnaround.alvor.common.logging;

import java.util.HashMap;
import java.util.Map;


public class Logs {
	private static Map<String, ILog> logMap = new HashMap<String, ILog>();
	public static ILog getLog(Class<?> clazz) {
		return getLog(clazz.getCanonicalName()); 
	}
	
	public static ILog getLog(String name) {
		if (!logMap.containsKey(name)) {
			logMap.put(name, new LogImpl(name));
		}
		return logMap.get(name);
	}
	
	
}
