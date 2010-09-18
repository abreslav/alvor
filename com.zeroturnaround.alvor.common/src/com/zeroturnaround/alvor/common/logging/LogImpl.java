package com.zeroturnaround.alvor.common.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class LogImpl implements ILog {
	
	private PrintStream fileStream;
	
	public LogImpl(Class<?> clazz) {
		IPath wsPath = Platform.isRunning()? ResourcesPlugin.getWorkspace().getRoot().getLocation() : new Path(".");
		IPath logFolder = wsPath.append(".metadata/.plugins/com.zeroturnaround.alvor.common/");
		File f = logFolder.append(clazz.getCanonicalName() + ".log").toFile();
		try {
			f.createNewFile(); // creates if it doesn't exist yet 
			fileStream = new PrintStream(f);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void exception(Throwable e) {
		e.printStackTrace(fileStream);
		fileStream.flush();
		
		e.printStackTrace(System.err);
		System.err.flush();
	}

	@Override
	public boolean format(String format, Object... args) {
		fileStream.format(format, args);
		fileStream.flush();
		
		System.out.format(format, args);
		System.out.flush();
		return true;
	}

	@Override
	public boolean message(Object message) {
		fileStream.println(message);
		fileStream.flush();
		
		System.out.println(message);
		System.out.flush();
		
		return true;
	}
	
	@Override
	public void error(Object message) {
		fileStream.println(message);
		System.err.println(message);
		
		if (message instanceof Throwable) {
			((Throwable) message).printStackTrace(fileStream);
			((Throwable) message).printStackTrace(System.err);
		}
		
		fileStream.flush();
		System.err.flush();
	}
	
	@Override
	protected void finalize() throws Throwable {
		fileStream.close();
	}
}
