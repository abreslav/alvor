package ee.stacc.productivity.edsl.common.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class AlternativeLog implements ILog {
	
	private PrintStream messageStream;
	
	public AlternativeLog(Class<?> clazz) {
		IPath wsPath = Platform.isRunning()? ResourcesPlugin.getWorkspace().getRoot().getLocation() : new Path(".");
		IPath logFolder = wsPath.append(".metadata/.plugins/ee.stacc.productivity.edsl.common/");
		File f = logFolder.append(clazz.getCanonicalName() + ".log").toFile();
		
		try {
			messageStream = new PrintStream(f);
		} catch (FileNotFoundException e) {
			messageStream = System.err;
		}
	}

	@Override
	public void exception(Throwable e) {
		e.printStackTrace(messageStream);
		messageStream.flush();
		
		e.printStackTrace(System.err);
		System.err.flush();
	}

	@Override
	public boolean format(String format, Object... args) {
		messageStream.format(format, args);
		messageStream.flush();
		
		System.out.format(format, args);
		System.out.flush();
		return true;
	}

	@Override
	public boolean message(Object message) {
		messageStream.println(message);
		messageStream.flush();
		
		System.out.println(message);
		System.out.flush();
		
		return true;
	}
	
	@Override
	public void error(Object message) {
		messageStream.println(message);
		messageStream.flush();
		
		System.err.println(message);
		System.err.flush();
		
	}
	
	@Override
	protected void finalize() throws Throwable {
		messageStream.close();
	}
}
