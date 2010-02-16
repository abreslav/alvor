package ee.stacc.productivity.edsl.common.logging;

public interface ILog {

	void message(Object message);
	void error(Object message);
	void exception(Throwable e);
	void format(String format, Object... args);
}
