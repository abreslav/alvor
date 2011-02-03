package com.zeroturnaround.alvor.common.logging;

public class Timer {

	private final ILog log;
	private long time;
	private String message;
	
	public Timer() {
		this(Logs.getLog(Timer.class));
	}

	public Timer(String message) {
		this(Logs.getLog(Timer.class), message);
	}
	
	public Timer(ILog log) {
		this.log = log;
	}
	
	public Timer(ILog log, String message) {
		this(log);
		start(message);
	}
	
	public final void start(String message) {
		this.message = message;
		this.time = System.nanoTime();
	}
	
	// To put into assert
	public boolean printTime() {
		long t = System.nanoTime() - time;
		log.message(message + ": " + (t / 1000000000.0));
		return false;
	}
	
	// To put into assert
	public boolean printTimeAndStart(String message) {
		printTime();
		start(message);
		return false;
	}
}
