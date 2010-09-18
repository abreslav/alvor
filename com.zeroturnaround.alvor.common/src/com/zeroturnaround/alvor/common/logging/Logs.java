package com.zeroturnaround.alvor.common.logging;


public class Logs {
	public static ILog getLog(Class<?> clazz) {
		return new LogImpl(clazz);
	}
}
