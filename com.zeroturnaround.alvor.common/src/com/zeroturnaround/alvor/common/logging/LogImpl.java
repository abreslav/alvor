package com.zeroturnaround.alvor.common.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.zeroturnaround.alvor.common.AlvorCommonPlugin;

public class LogImpl implements ILog {
	
	private PrintStream fileStream;
	
	public LogImpl(String name) {
		IPath logFolder = AlvorCommonPlugin.getDefault().getStateLocation();
		File f = logFolder.append(name + ".log").toFile();
		try {
			f.createNewFile(); // creates if it doesn't exist yet 
			fileStream = new PrintStream(f);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void exception(Throwable e) {
		this.error("EXCEPTION", e);
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
	protected void finalize() throws Throwable {
		fileStream.close();
	}

	@Override
	public void error(String message, Throwable e) {
		fileStream.println(message);
		System.err.println(message);
		
		if (e != null) {
			e.printStackTrace(fileStream);
			e.printStackTrace(System.err);
		}
		
		// add also to eclipse log
		AlvorCommonPlugin.getDefault().getLog().log(
				new Status(IStatus.ERROR, AlvorCommonPlugin.ID, message, e));
		
		fileStream.flush();
		System.err.flush();
	}
}
