package ee.stacc.productivity.edsl.common.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class PrintStreamLog implements ILog {

	public static final ILog SYSTEM_OUT = new PrintStreamLog(System.out, System.err);
	public static final ILog SYSTEM_ERR = new PrintStreamLog(System.err, System.err);
	
	private final PrintStream messageStream;
	private final PrintStream errorStream;
	private PrintStream tempBackupStream;
	
	public PrintStreamLog(PrintStream messageStream, PrintStream errorStream) {
		this.messageStream = messageStream;
		this.errorStream = errorStream;
		
		tempBackupStream = null;
		if (new File("C:/bundle/").isDirectory()) {
			try {
				tempBackupStream = new PrintStream(new File("c:/bundle/esql.log"));
			} catch (FileNotFoundException e) {
					e.printStackTrace();
			}
		}
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
		
		if (tempBackupStream != null) {
			tempBackupStream.println(message);
			tempBackupStream.flush();
		}
		
		return true;
	}
	
	@Override
	public void error(Object message) {
		errorStream.println(message);
		errorStream.flush();
		
		if (tempBackupStream != null) {
			tempBackupStream.println(message);
			tempBackupStream.flush();
		}
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		messageStream.close();
		errorStream.close();
		
		if (tempBackupStream != null) {
			tempBackupStream.close();
		}
		
	}

}
