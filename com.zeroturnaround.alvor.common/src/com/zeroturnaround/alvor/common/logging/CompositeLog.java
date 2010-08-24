package com.zeroturnaround.alvor.common.logging;

public class CompositeLog implements ILog {

	private final ILog[] sublogs;
	
	public CompositeLog(ILog... sublogs) {
		this.sublogs = sublogs.clone();
	}

	@Override
	public void exception(Throwable e) {
		for (ILog log : sublogs) {
			log.exception(e);
		}
	}

	@Override
	public boolean format(String format, Object... args) {
		for (ILog log : sublogs) {
			log.format(format, args);
		}
		return true;
	}

	@Override
	public boolean message(Object message) {
		for (ILog log : sublogs) {
			log.message(message);
		}
		return true;
	}

	@Override
	public void error(Object message) {
		for (ILog log : sublogs) {
			log.error(message);
		}
	}

}
