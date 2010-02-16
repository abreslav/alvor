package ee.stacc.productivity.edsl.common.logging;

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
	public void format(String format, Object... args) {
		for (ILog log : sublogs) {
			log.format(format, args);
		}
	}

	@Override
	public void message(Object message) {
		for (ILog log : sublogs) {
			log.message(message);
		}
	}

	@Override
	public void error(Object message) {
		for (ILog log : sublogs) {
			log.error(message);
		}
	}

}
