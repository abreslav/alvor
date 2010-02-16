package ee.stacc.productivity.edsl.common.logging;

import java.io.PrintStream;

public class PrintStreamLog implements ILog {

	public static final ILog SYSTEM_OUT = new PrintStreamLog(System.out);
	public static final ILog SYSTEM_ERR = new PrintStreamLog(System.err);
	
	private final PrintStream printStream;
	
	public PrintStreamLog(PrintStream printStream) {
		this.printStream = printStream;
	}

	@Override
	public void exception(Throwable e) {
		e.printStackTrace(printStream);
		printStream.flush();
	}

	@Override
	public void format(String format, Object... args) {
		printStream.format(format, args);
		printStream.flush();
	}

	@Override
	public void message(Object message) {
		printStream.println(message);
		printStream.flush();
	}
	
	@Override
	public void error(Object message) {
		message(message);
	}
	
	@Override
	protected void finalize() throws Throwable {
		printStream.close();
	}

}
