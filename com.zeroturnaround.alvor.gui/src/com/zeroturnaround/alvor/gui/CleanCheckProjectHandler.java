package com.zeroturnaround.alvor.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;

import com.zeroturnaround.alvor.cache.CacheService;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.crawler.NodeSearchEngine;

public class CleanCheckProjectHandler extends AbstractHandler {
	GuiChecker checker = new GuiChecker();
	private static final ILog LOG = Logs.getLog(CleanCheckProjectHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("MEM: totalMemory() == " + Runtime.getRuntime().totalMemory());
		System.out.println("MEM: maxMemory() == " + Runtime.getRuntime().maxMemory());
		System.out.println("MEM: freeMemory() == " + Runtime.getRuntime().freeMemory());
		System.out.println("MEM: used memory == " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		
		Timer timer = new Timer();
		timer.start("TIMER: whole process");
		assert LOG.message("CheckProjectHandler.execute");
		
		try {
			IJavaElement element = GuiUtil.getSingleSelectedJavaElement();
			checker.performCleanCheck(element, new IJavaElement[] {element});
		} 
		catch (IllegalAccessException e) {
			GuiUtil.ShowErrorDialog(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		timer.printTime();
		return null; // Must be null
	}
}
