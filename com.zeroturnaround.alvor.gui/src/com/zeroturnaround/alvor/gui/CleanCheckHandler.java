package com.zeroturnaround.alvor.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
//import com.zeroturnaround.alvor.common.logging.Timer;

public class CleanCheckHandler extends AbstractHandler {
	GuiChecker checker = new GuiChecker();
	private static final ILog LOG = Logs.getLog(CleanCheckHandler.class);
	

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
//			assert LOG.message("MEM: totalMemory() == " + Runtime.getRuntime().totalMemory());
//			assert LOG.message("MEM: maxMemory() == " + Runtime.getRuntime().maxMemory());
//			assert LOG.message("MEM: freeMemory() == " + Runtime.getRuntime().freeMemory());
//			assert LOG.message("MEM: used memory == " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//			Timer timer = new Timer();
//			timer.start("TIMER: whole process");
			
			// first expect right-click in package explorer
			IJavaElement element = GuiUtil.getSingleSelectedJavaElement();
			// fall back to other means (like active editor)
			if (element == null) {
				element = GuiUtil.getCurrentJavaProject();
			}
			LOG.message("Checking project: " + element.getElementName());
			checker.performCleanCheck(element.getJavaProject().getProject(), new IJavaElement[] {element});
			
//			timer.printTime();
		} 
		catch (Exception e) {
			LOG.exception(e);
		}
		
		return null;
	}

}
