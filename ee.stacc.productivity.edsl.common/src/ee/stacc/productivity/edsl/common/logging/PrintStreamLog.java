package ee.stacc.productivity.edsl.common.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class PrintStreamLog implements ILog {

	public static final ILog SYSTEM_OUT = new PrintStreamLog(System.out, System.err);
	public static final ILog SYSTEM_ERR = new PrintStreamLog(System.err, System.err);
	
	private final PrintStream messageStream;
	private final PrintStream errorStream;
	
	public PrintStreamLog(PrintStream messageStream, PrintStream errorStream) {
		this.messageStream = messageStream;
		this.errorStream = errorStream;
	}
	
	public PrintStreamLog(PrintStream messageStream) {
		this(messageStream, messageStream);
	}

	@Override
	public void exception(Throwable e) {
		e.printStackTrace(errorStream);
		errorStream.flush();
	}

	@Override
	public boolean format(String format, Object... args) {
		messageStream.format(format, args);
		messageStream.flush();
		return true;
	}

	@Override
	public boolean message(Object message) {
		messageStream.println(message);
		messageStream.flush();
		
		return true;
	}
	
	@Override
	public void error(Object message) {
		errorStream.println(message);
		errorStream.flush();
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		messageStream.close();
		errorStream.close();
	}

}
