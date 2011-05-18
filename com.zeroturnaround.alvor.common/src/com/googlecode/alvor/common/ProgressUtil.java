package com.googlecode.alvor.common;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Enables using progress monitors without null-checks
 * @author Aivar
 *
 */
public class ProgressUtil {
	public static void beginTask(IProgressMonitor monitor, String name, int totalWork) {
		if (monitor != null) {
			monitor.beginTask(name, totalWork);
			monitor.setTaskName(name); // is it necessary?
		}
	}
	
	public static void done(IProgressMonitor monitor) {
		if (monitor != null) {
			monitor.done();
		}
	}
	
	public static void worked(IProgressMonitor monitor, int work) {
		if (monitor != null) {
			monitor.worked(work);
		}
	}
	
	public static void checkAbort(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}
	
	public static IProgressMonitor subMonitor(IProgressMonitor monitor, int ticks) {
		if (monitor != null) {
			return new SubProgressMonitor(monitor, ticks);
		}
		else {
			return null;
		}
	}
}
