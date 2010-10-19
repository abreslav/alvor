package com.zeroturnaround.alvor.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Timer;

public class CleanCheckProjectHandler extends AbstractHandler {
	GuiChecker checker = new GuiChecker();
	private static final ILog LOG = Logs.getLog(CleanCheckProjectHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		assert LOG.message("MEM: totalMemory() == " + Runtime.getRuntime().totalMemory());
		assert LOG.message("MEM: maxMemory() == " + Runtime.getRuntime().maxMemory());
		assert LOG.message("MEM: freeMemory() == " + Runtime.getRuntime().freeMemory());
		assert LOG.message("MEM: used memory == " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		
		Timer timer = new Timer();
		timer.start("TIMER: whole process");
		assert LOG.message("CheckProjectHandler.execute");
		
		try {
			IJavaElement element = GuiUtil.getSingleSelectedJavaElement();
			checker.performCleanCheck(element, new IJavaElement[] {element});
		} 
		catch (IllegalAccessException e) {
			String msg = e.getMessage();
			Throwable cause = e.getCause();
			if (cause != null) {
				msg = cause.getMessage();
			}
			GuiUtil.ShowErrorDialog(msg, e);
		}
		catch (Exception e) {
			LOG.exception(e);
		}
		
		timer.printTime();
		return null; // Must be null
	}
}
