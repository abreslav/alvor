package com.googlecode.alvor.common.logging;

public interface ILog {

	/**
	 * @return always returns {@code true}
	 */
	boolean message(Object message);  // should be put into assert
	
	/**
	 * @return always returns {@code true}
	 */
	boolean format(String format, Object... args);

	void error(String message, Throwable e);
	void error(String message);
	void exception(Throwable e);
}
